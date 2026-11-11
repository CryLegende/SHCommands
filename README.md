<div align="center">

<img src="assets/logo.png" alt="SkyHand" width="140">

# SHCommands

*Commandes utilitaires : /sm envoie un message coloré à un joueur, /ps lui joue un son, /ega donne des items depuis un menu ou lance une commande pour tous les joueurs.*

<img src="https://img.shields.io/badge/Paper-26.3-f2a900?style=for-the-badge" alt="Paper 26.3"> <img src="https://img.shields.io/badge/Java-25-e76f00?style=for-the-badge" alt="Java 25"> <img src="https://img.shields.io/badge/SkyHand-Opale-3aa655?style=for-the-badge" alt="SkyHand Opale">

</div>

---

## Présentation

Commandes utilitaires : /sm envoie un message coloré à un joueur, /ps lui joue un son, /ega (ou /easygiveall) donne des items depuis un menu ou lance une commande pour tous les joueurs connectés. Remplace EasyGiveAll.

Utilisé sur : **Opale**.

### /ega

- **Sans argument** : un menu où le staff se donne des items (clés de box, chèques, spawners...). Il clique un item, répond dans le chat à chaque paramètre demandé (quantité, type...), donne une raison, et la commande de l'item part de la console. Chaque give est écrit dans `plugins/SHCommands/logs/<jj-mm-aaaa>.log`. Les items, leurs paramètres et leurs limites se règlent dans `easygiveall.yml`.
- **Avec une commande** : `/ega give %player% diamond 1` lance `give <pseudo> diamond 1` depuis la console pour chaque joueur connecté. `%player%` devient son pseudo et `%me%` celui du staff. Réservé aux joueurs : la console est refusée.
- **`/ega reload`** relit `easygiveall.yml` et `messages.yml`.

### Mode réseau

Avec **SHNetwork** (facultatif), un joueur connecté sur un autre serveur du réseau reçoit le message, le son ou la commande de `/ega` là où il se trouve : la commande est exécutée par la console de son serveur, une seule fois, et les placeholders du message sont résolus pour lui. Pour `/ega <commande>`, les joueurs de ce serveur la reçoivent d'ici, ceux des autres serveurs par `runFor` de SHNetwork (attendue jusqu'à ce que le joueur soit prêt, gardée jusqu'à sa prochaine connexion s'il est en transit). Le menu reste local : le staff se donne les items sur le serveur où il se trouve. Sans SHNetwork, ou avec `reseau.actif: false`, seuls les joueurs du serveur sont atteints.

## Commandes

| Commande | Alias | Description | Permission |
|---|---|---|---|
| `/sm <joueur> <message>` | — | Envoie un message coloré à un joueur | `shcommands.use` |
| `/ps <joueur> <son> [volume] [pitch]` | — | Joue un son pour un joueur | `shcommands.use` |
| `/easygiveall` | `/ega` | Menu de give du staff, ou commande lancée pour chaque joueur connecté | `easygiveall.givemenu` |
| `/shcommands reload` | — | Relit `config.yml`, `messages.yml` et `easygiveall.yml`. Si l'un des trois est invalide, rien n'est remplacé. | `shcommands.admin.reload` |

## Permissions

| Permission | Description | Par défaut |
|---|---|---|
| `shcommands.use` | Permet d'utiliser /sm et /ps | `op` |
| `shcommands.admin` | Toutes les sous-commandes de /shcommands | `op` |
| `shcommands.admin.reload` | `/shcommands reload` | `op` |
| `easygiveall.givemenu` | Ouvre le menu de /ega | `op` |
| `easygiveall.giveall` | `/ega <commande>` : lance la commande pour chaque joueur connecté | `op` |
| `easygiveall.reload` | `/ega reload` | `op` |
| `easygiveall.*` | Les trois permissions de /ega | — |

## Configuration

Fichiers par défaut :

<pre>
plugins/SHCommands/
├── <a href="src/main/resources/config.yml">config.yml</a>
├── <a href="src/main/resources/easygiveall.yml">easygiveall.yml</a>
├── <a href="src/main/resources/messages.yml">messages.yml</a>
└── logs/
    └── jj-mm-aaaa.log
</pre>

`easygiveall.yml` est créé au premier démarrage s'il manque : réglages du menu (`menu.title`, `menu.time-limit`, `menu.cancel-word`, `menu.logs`) et items (`items.<id>` : nom, icône, commande, paramètres). Son en-tête décrit chaque champ.

La clé `reseau.actif` (par défaut `true`) active l'envoi vers les autres serveurs quand SHNetwork est présent. Elle se lit au démarrage.

## Compilation

Java 25 et Maven :

```bash
mvn clean package
```

Le jar est généré dans `target/`.

---

<div align="center">

<sub>Développé par <b>Cry_Legende</b> pour le serveur <b>SkyHand</b> · Tous droits réservés</sub>

</div>
