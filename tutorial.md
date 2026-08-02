# 🚀 BBL Routers — Comprehensive Tutorial & Guide

**BBL Routers** is a lightweight, high-performance wireless resource transfer mod for Minecraft 1.21.1 (NeoForge). It allows you to wirelessly route **Items**, **Fluids**, **Energy (RF/FE)**, **Chemicals (Mekanism)**, **Source (Ars Nouveau)**, **Souls (Industrial Foregoing)**, **Pressure (PneumaticCraft)**, and **Heat** across any distance and even between different dimensions!

---

## 📋 Table of Contents
1. [Core Concepts & Mechanics](#1-core-concepts--mechanics)
2. [Key Blocks & Tools](#2-key-blocks--tools)
3. [Step-by-Step Setup Guide](#3-step-by-step-setup-guide)
4. [Upgrades Reference](#4-upgrades-reference)
5. [Filtering System](#5-filtering-system)
6. [Visual Debugging & Connection Beams](#6-visual-debugging--connection-beams)
7. [Troubleshooting & Pro Tips](#7-troubleshooting--pro-tips)

---

## 1. Core Concepts & Mechanics

* **Extractor / Exporter Block**: Placed next to a **source container/machine**. It extracts resources from the container it faces and wirelessly sends them to connected Importers.
* **Importer Block**: Placed next to a **target container/machine**. It receives resources from connected Exporters and inserts them into the container it faces.
* **Upgrades**: Placed inside the Exporter to define **what** is transferred (Items, Fluids, Energy, etc.), **how fast**, and **how much**.
* **Filters**: Placed in the Exporter or Importer GUI to whitelist specific items, tags, mods, or fluids/chemicals.

> [!IMPORTANT]
> **Facing Matters!**  
> * The **Exporter** must be placed facing the block you want to **extract from**.
> * The **Importer** must be placed facing the block you want to **insert into**.

---

## 2. Key Blocks & Tools

| Block / Item | Function |
| :--- | :--- |
| 📤 **Exporter Block** | Extracts from adjacent inventory/machine and wirelessly transmits to Importers. Holds upgrades and filters. |
| 📥 **Importer Block** | Receives wireless transmissions and inserts into adjacent inventory/machine. Holds filters. |
| 🔗 **Router Connector** | Tool used to link Importers to Exporters. |
| 🏷️ **Tag Filter / Mod Filter** | Special filter items used to filter items by tag (e.g. `c:ores`) or mod ID (e.g. `minecraft`). |
| 🛠️ **Wrench (`c:tools/wrench`)** | Holding any wrench in your main hand renders visible laser connection beams in the world! |

---

## 3. Step-by-Step Setup Guide

Follow these steps to establish your first wireless item/resource transport line:

### Step 1: Place the Importer
1. Place an **Importer Block** adjacent to your target container (e.g. a Chest or Furnace).
2. Ensure the front face of the Importer is pointing **towards** the container.

### Step 2: Place the Exporter
1. Place an **Exporter Block** adjacent to your source container (e.g. a Quarry or Chest).
2. Ensure the front face of the Exporter is pointing **towards** the source container.

### Step 3: Link Importer to Exporter using the Router Connector
1. Grab a **Router Connector** item.
2. **Right-Click** on the **Importer Block**. *(You will see a confirmation message and the coordinates will be saved to the connector)*.
   * *Tip: Hold `Shift` while hovering over the connector in your inventory to see the saved Importer coordinates.*
3. **Right-Click** on the **Exporter Block** with the connector. *(Actionbar message will display: "Importer Added")*.
   * *Note: You can connect multiple Importers to a single Exporter! Right-clicking again will toggle/remove the connection.*

### Step 4: Insert the Required Upgrade
1. Open the **Exporter GUI** by right-clicking it.
2. Insert an **Item Upgrade** (or Fluid/RF/Chemical Upgrade depending on what you want to transfer) into one of the upgrade slots.
3. Your transfer line is now active! 🎉

---

## 4. Upgrades Reference

Upgrades are inserted into the **Exporter**'s upgrade slots. Without a specific transfer upgrade, the Exporter will not transfer that resource type.

### Resource Upgrades

| Upgrade Type | Tiers Available | Default Transfer Rates (Configurable) |
| :--- | :--- | :--- |
| 📦 **Item Upgrade** | Tier 1 – 4 | **T1:** 1 item/op \| **T2:** 8 items/op \| **T3:** 32 items/op \| **T4:** 64 items/op |
| 💧 **Fluid Upgrade** | Tier 1 – 4 | **T1:** 100 mB \| **T2:** 1,000 mB \| **T3:** 10,000 mB \| **T4:** 100,000 mB |
| ⚡ **RF / Energy Upgrade** | Tier 1 – 4 | **T1:** 10 RF/t \| **T2:** 240 RF/t \| **T3:** 12,000 RF/t \| **T4:** 50,000 RF/t |
| 🧪 **Chemical Upgrade** *(Mekanism)* | Tier 1 – 4 | **T1:** 10 mB \| **T2:** 100 mB \| **T3:** 1,000 mB \| **T4:** 10,000 mB |
| 🔮 **Source Upgrade** *(Ars Nouveau)* | Tier 1 – 4 | **T1:** 10 \| **T2:** 100 \| **T3:** 500 \| **T4:** 1,000 source |
| 👻 **Soul Upgrade** *(Industrial Foregoing)* | Tier 1 – 4 | Transfers soul energy between Soul Networks. |
| 💨 **Pressure Upgrade** *(PneumaticCraft)* | Tier 1 – 4 | Transfers air pressure. |
| 🌡️ **Heat Upgrade** *(PneumaticCraft)* | Tier 1 – 4 | Transfers heat. |

### Special & Utility Upgrades

| Upgrade | Function |
| :--- | :--- |
| ⚡ **Speed Upgrade** | Decreases the delay between transfer operations.<br>• *No Upgrade:* Operation every **40 ticks** (2 seconds)<br>• *Tier 1:* Every **30 ticks**<br>• *Tier 2:* Every **20 ticks** (1 second)<br>• *Tier 3:* Every **10 ticks**<br>• *Tier 4:* Every **1 tick** (20 operations/sec!) |
| 🔄 **Round Robin Upgrade** | Evenly distributes transferred items/fluids/energy sequentially among all connected Importers instead of filling the first one first. |
| 🌌 **Dimensional Upgrade** | Enables cross-dimensional transfers (e.g., Nether to Overworld or End). |

---

## 5. Filtering System

Both Exporters and Importers feature an 18-slot filter grid in their GUIs.

* **Exporter Filters**: Restrict what can be **extracted** from the source container.
* **Importer Filters**: Restrict what can be **received** by the target container.

### Filter Types Supported:
1. **Direct Item Filter**: Place any item directly into the filter slots.
2. **Tag Filter**: Right-click a **Tag Filter** item in your hand to configure an item tag (e.g. `c:ores`, `c:ingots/iron`).
3. **Mod Filter**: Right-click a **Mod Filter** item in your hand to specify a Mod ID (e.g. `mekanism`, `ars_nouveau`, `minecraft`).
4. **Fluid & Chemical Filters**: Configure fluid and chemical whitelist stacks via the GUI fluid/chemical filter slots.

---

## 6. Visual Debugging & Connection Beams

BBL Routers includes a built-in visual connection renderer to help you debug and inspect your network in real-time.

### How to activate visual beams:
Hold any **Wrench** (any item in the `c:tools/wrench` tag) in your **Main Hand**.

Animated laser beams will render from the Exporter to all connected Importers, color-coded by active upgrade:

* 🔴 **Red Beam**: RF / Energy Transfer active
* ⚪ **Grey Beam**: Item Transfer active
* 🔵 **Blue Beam**: Fluid Transfer active
* 🟣 **Purple Beam**: Chemical Transfer active *(Mekanism)*
* 🩵 **Light Blue / Cyan Beam**: Soul Transfer active *(Industrial Foregoing)*
* 🩷 **Pink / Magenta Beam**: Source Transfer active *(Ars Nouveau)*
* 🟢 **Green / Teal Beam**: Pressure Transfer active *(PneumaticCraft)*

---

## 7. Troubleshooting & Pro Tips

> [!WARNING]
> **Chunk Loading is Essential for Long Distances!**  
> The Exporter checks connected Importers every 10 ticks. If an Importer is located in an **unloaded chunk** (e.g. far away across your base or in another dimension), Minecraft returns `null` for the block entity.  
> **Always chunk-load both the Exporter and Importer blocks** (using FTB Chunks, Claim Chunks, or Anchor blocks), otherwise the Exporter will automatically remove the connection to the unloaded Importer!

### Quick Troubleshooting Checklist:
1. **Items are not transferring?**
   * Did you place an **Item Upgrade** inside the Exporter? (It won't transfer items without it!)
   * Is the Exporter facing the source container?
   * Is the Importer facing the target container?
   * Is the target container full?
2. **Transfer is too slow?**
   * Add **Speed Upgrades** to reduce the operation interval down to 1 tick.
   * Upgrade your **Item / Fluid / Energy Upgrade** to Tier 4 to transfer larger stack sizes per tick.
3. **Not working across dimensions?**
   * Make sure you have inserted a **Dimensional Upgrade** into the Exporter.
   * Make sure both chunks are **chunk-loaded**.
