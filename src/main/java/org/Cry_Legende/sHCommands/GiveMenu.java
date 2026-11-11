package org.Cry_Legende.sHCommands;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Menu /ega : le staff clique un item, répond dans le chat à chaque paramètre de la commande puis donne une raison ;
 * la commande part alors de la console et le give est écrit dans le journal. Tout se passe sur le serveur du staff.
 */
final class GiveMenu implements Listener {

    private final SHCommands plugin;
    private final Messages messages;
    private final GiveLog log;
    private volatile GiveSettings settings;
    private final Map<UUID, Sequence> sequences = new ConcurrentHashMap<>();

    GiveMenu(SHCommands plugin, Messages messages, GiveSettings settings) {
        this.plugin = plugin;
        this.messages = messages;
        this.log = new GiveLog(plugin);
        this.settings = settings;
    }

    void setSettings(GiveSettings settings) {
        this.settings = settings;
    }

    int itemCount() {
        return settings.items().size();
    }

    /** Vrai si le joueur est en train de se donner un item (réponses attendues dans le chat). */
    boolean busy(Player player) {
        return sequences.containsKey(player.getUniqueId());
    }

    // ------------------------------------------------------------------ menu

    void open(Player player) {
        GiveSettings current = settings;
        List<GiveItem> items = current.items();
        Holder holder = new Holder(items);
        int size = Math.min(54, Math.max(9, (items.size() + 8) / 9 * 9));
        Inventory inventory = Bukkit.createInventory(holder, size, GiveItem.text(current.title()));
        holder.inventory = inventory;

        ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = filler.getItemMeta();
        meta.displayName(Component.space());
        filler.setItemMeta(meta);
        for (int slot = 0; slot < size; slot++) {
            inventory.setItem(slot, slot < items.size() ? items.get(slot).icon().clone() : filler);
        }
        player.openInventory(inventory);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        Inventory top = event.getView().getTopInventory();
        if (!(top.getHolder(false) instanceof Holder holder)) return;
        event.setCancelled(true);
        if (event.getClickedInventory() != top) return;
        int slot = event.getSlot();
        if (slot < 0 || slot >= holder.items.size()) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;
        GiveItem item = holder.items.get(slot);
        Bukkit.getScheduler().runTask(plugin, () -> {
            player.closeInventory();
            if (busy(player)) {
                player.sendMessage(messages.get("ega.already-give"));
                return;
            }
            begin(player, item);
        });
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder(false) instanceof Holder) {
            event.setCancelled(true);
        }
    }

    // ------------------------------------------------------------------ saisie dans le chat

    /** Fil principal. */
    private void begin(Player player, GiveItem item) {
        if (!player.isOnline()) return;
        Sequence sequence = new Sequence(item);
        sequences.put(player.getUniqueId(), sequence);
        advance(player, sequence);
    }

    /** Les réponses du staff ne sortent pas dans le chat. Même priorité que les autres saisies par chat de SH. */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        Sequence sequence = sequences.get(player.getUniqueId());
        if (sequence == null) return;
        event.setCancelled(true);
        String message = event.getMessage();
        Bukkit.getScheduler().runTask(plugin, () -> answer(player, sequence, message));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Sequence sequence = sequences.remove(event.getPlayer().getUniqueId());
        if (sequence != null) sequence.stop();
    }

    /** Fil principal : traite une réponse du staff. */
    private void answer(Player player, Sequence sequence, String message) {
        if (sequences.get(player.getUniqueId()) != sequence) return;
        String text = message.trim();
        if (text.equalsIgnoreCase(settings.cancel())) {
            end(player, sequence);
            player.sendMessage(messages.get("ega.cancelled"));
            return;
        }
        if (sequence.pending.isEmpty()) {
            sequence.reason = text;
            advance(player, sequence);
            return;
        }
        String name = sequence.pending.peekFirst();
        String value = sequence.item.modifiers().get(name).accept(text);
        if (value == null) {
            player.sendMessage(messages.get("ega.todo-notValid"));
            prompt(player, sequence, name);
            return;
        }
        sequence.command = sequence.command.replace("%" + name + "%", value);
        sequence.pending.removeFirst();
        advance(player, sequence);
    }

    /** Remplit ce qui peut l'être seul (SELF), puis demande le paramètre suivant, la raison, ou lance la commande. */
    private void advance(Player player, Sequence sequence) {
        while (!sequence.pending.isEmpty()) {
            String name = sequence.pending.peekFirst();
            if (sequence.item.modifiers().get(name).type() != Modifier.Type.SELF) {
                prompt(player, sequence, name);
                return;
            }
            sequence.command = sequence.command.replace("%" + name + "%", player.getName());
            sequence.pending.removeFirst();
        }
        if (sequence.reason == null) {
            player.sendMessage(messages.get("ega.todo-REASON"));
            arm(player, sequence);
            return;
        }
        finish(player, sequence);
    }

    private void prompt(Player player, Sequence sequence, String name) {
        Modifier modifier = sequence.item.modifiers().get(name);
        String label = Character.toUpperCase(name.charAt(0)) + name.substring(1);
        switch (modifier.type()) {
            case NONE -> player.sendMessage(messages.get("ega.todo-NONE", "%modifier%", label));
            case ANY -> player.sendMessage(messages.get("ega.todo-ANY", "%modifier%", label));
            case RANGE -> player.sendMessage(messages.get("ega.todo-RANGE", "%modifier%", label,
                "%min%", String.valueOf(modifier.min()), "%max%", String.valueOf(modifier.max())));
            case CONNECTED -> player.sendMessage(messages.get("ega.todo-CONNECTED", "%modifier%", label));
            case VALUES -> player.sendMessage(messages.get("ega.todo-VALUES", "%values%", modifier.allowed()));
            default -> { }
        }
        arm(player, sequence);
    }

    private void finish(Player player, Sequence sequence) {
        end(player, sequence);
        String command = GiveAll.clean(sequence.command);
        if (settings.logs()) {
            log.add(player.getName(), sequence.item, command, sequence.reason);
        }
        player.sendMessage(messages.get("ega.item-given", "%item%", GiveItem.legacy(sequence.item.name())));
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
    }

    private void end(Player player, Sequence sequence) {
        sequences.remove(player.getUniqueId(), sequence);
        sequence.stop();
    }

    /** Relance le délai : sans réponse pendant time-limit secondes, la saisie est abandonnée. */
    private void arm(Player player, Sequence sequence) {
        sequence.stop();
        sequence.timeout = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (sequences.remove(player.getUniqueId(), sequence) && player.isOnline()) {
                player.sendMessage(messages.get("ega.cancelled"));
            }
        }, settings.timeLimit() * 20L);
    }

    /** Un give en cours : l'item choisi, la commande remplie au fil des réponses, les paramètres restants. */
    private static final class Sequence {
        final GiveItem item;
        final Deque<String> pending;
        String command;
        String reason;
        BukkitTask timeout;

        Sequence(GiveItem item) {
            this.item = item;
            this.command = item.command();
            this.pending = new ArrayDeque<>(item.parameters());
        }

        void stop() {
            if (timeout != null) {
                timeout.cancel();
                timeout = null;
            }
        }
    }

    /** Reconnaît le menu et garde les items tels qu'ils étaient à son ouverture. */
    private static final class Holder implements InventoryHolder {
        final List<GiveItem> items;
        Inventory inventory;

        Holder(List<GiveItem> items) {
            this.items = items;
        }

        @Override
        public Inventory getInventory() {
            return inventory;
        }
    }
}
