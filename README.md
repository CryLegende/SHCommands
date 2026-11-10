<div align="center">

<img src="assets/logo.png" alt="SkyHand" width="140">

# SHCommands

*Commandes utilitaires : /sm envoie un message coloré à un joueur, /ps lui joue un son.*

<img src="https://img.shields.io/badge/Paper-26.3-f2a900?style=for-the-badge" alt="Paper 26.3"> <img src="https://img.shields.io/badge/Java-25-e76f00?style=for-the-badge" alt="Java 25"> <img src="https://img.shields.io/badge/SkyHand-Opale-3aa655?style=for-the-badge" alt="SkyHand Opale">

</div>

---

## Présentation

Commandes utilitaires : /sm envoie un message coloré à un joueur, /ps lui joue un son.

Utilisé sur : **Opale**.

Avec **SHNetwork** (facultatif), un joueur connecté sur un autre serveur du réseau reçoit le message ou le son là où il se trouve : la commande est exécutée par la console de son serveur, une seule fois, et les placeholders du message sont résolus pour lui. Sans SHNetwork, ou avec `reseau.actif: false`, seuls les joueurs du serveur sont atteints.

## Commandes

| Commande | Alias | Description | Permission |
|---|---|---|---|
| `/sm` | — | Envoie un message coloré à un joueur | — |
| `/ps` | — | Joue un son pour un joueur | — |

## Permissions

| Permission | Description | Par défaut |
|---|---|---|
| `shcommands.use` | Permet d'utiliser les commandes SHCommands | `op` |

## Configuration

Fichiers par défaut :

<pre>
plugins/SHCommands/
├── <a href="src/main/resources/config.yml">config.yml</a>
└── <a href="src/main/resources/messages.yml">messages.yml</a>
</pre>

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
