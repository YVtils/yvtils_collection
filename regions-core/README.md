# YVtils Regions

YVtils Regions is a plugin for survival servers that allows players to create and manage their own regions. It supports customizable flags with global or role-based permission levels for fine-grained control.

This folder contains the regions-core module, which is the core of the plugin and is used for handling the completly bottom of logic for starting and stopping the plugin.
For the logic you will need to open the [regions module](https://github.com/YVtils/yvtils_collection/tree/main/regions).

## Included modules

* **regions-core**: The core module that handles the basic logic of the plugin.
* **regions**: The main module that handles the region management and permissions.
* **common**: A module that contains base logic that is included in every yvtils plugin like the version check and the player language logic
* **config**: A configuration module that manages the plugin's settings and options.
* **utils**: A utility module that provides helper functions and classes for the plugin.

---

![Plugin Banner](https://cdn.modrinth.com/data/NTJG4sRk/images/57bd37cebc4fc68c6ec3074f4d8f2b04ecd522fe.png)

# YVtils Regions – Claim your territory!

> ⚠️ **Disclaimer:** This project is currently in beta. Features, configurations, and flags are actively being expanded and improved. Expect updates and enhancements in the near future!

> Take full control over your land – with regions, flags, and fine-grained permissions!

---

### This Plugin does **not** support Spigot or Minecraft versions < 1.21.1

## Key Features

* Claim regions with simple commands
* Set flags with multiple permission levels
* Region visibility and trust system
* Integrates with other YVtils modules

---

## 🔧 How It Works

Players can:

* Create, expand, and delete their own **regions**
* Assign **permissions and flags** for groups, or globally
* Trust other players or revoke access at any time

---

## 🎬 Showcase

WIP

---

## 🗜️ Regions Overview

* **Small and large regions** are supported
* Works with **buildings and terrain**

---

## 📜 Commands

```
/region create <regionName> <corner1 (x, z)> <corner2 (x, z)>
/region delete <regionName>
/region flags <flag> <value> [regionName]
/region members <add/remove/role> <player> [role]
/region info [regionName]
/region list [role] [player]
```

---

## 🔐 Permissions & Flags

WIP

---

## 🌍 Localization

The plugin supports:

* **English**
* **Deutsch**

Players will automatically see messages in their game language if supported.

---

## 🛠️ Installation

1. Download the plugin `.jar` from Modrinth or GitHub
2. Place it in your `plugins/` folder
3. Restart the server

---

## 📬 Feedback

* [Request a Feature](https://github.com/YVtils/yvtils_collection/issues/new?template=feature_request_regions.md)
* [Report a Bug](https://github.com/YVtils/yvtils_collection/issues/new?template=bug_report_regions.md)
* [Join our Discord](https://yvtils.net/yvtils/support)
