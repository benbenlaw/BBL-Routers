# 📦 BBL Routers

[![Minecraft Version](https://img.shields.io/badge/Minecraft-1.21.1-brightgreen.svg)](https://minecraft.net/)
[![Mod Loader](https://img.shields.io/badge/ModLoader-NeoForge-orange.svg)](https://neoforged.net/)

**BBL Routers** is a high-performance, wireless resource transportation mod for Minecraft 1.21.1 (NeoForge). It allows players to route **Items**, **Fluids**, **Energy (RF/FE)**, **Chemicals**, **Source**, **Souls**, **Pressure**, and **Heat** wirelessly between containers across any distance and dimension.

---

## ✨ Features

- 📤 **Wireless Resource Routing**: Transfer items, fluids, energy, and modded resources without cluttering your base with pipes.
- 🔗 **Simple Linking**: Pair Exporters and Importers in seconds using the **Router Connector**.
- 🔄 **Round-Robin Support**: Evenly distribute items across multiple target destinations.
- 🌌 **Cross-Dimensional Routing**: Seamlessly transfer resources between the Overworld, Nether, End, or custom mod dimensions.
- 👁️ **Visual Debug Beams**: Hold any Wrench in your main hand to visualize active connections with color-coded laser beams in real-time!
- 🏷️ **Advanced Filtering**: Filter by exact items, NeoForge Item Tags (e.g. `c:ores`), Mod IDs, or fluid/chemical types.
- 🔌 **Extensive Mod Compatibility**:
  - **Mekanism** *(Chemicals)*
  - **Ars Nouveau** *(Source)*
  - **Industrial Foregoing Souls** *(Souls)*
  - **PneumaticCraft: Repressurized** *(Air Pressure & Heat)*

---

## ⚡ Quick Start

1. **Place Importer**: Position an **Importer Block** facing the destination container (e.g., chest or machine).
2. **Place Exporter**: Position an **Exporter Block** facing the source container.
3. **Link**: Right-click the **Importer** with a **Router Connector**, then right-click the **Exporter** to pair them.
4. **Add Upgrade**: Insert an **Item Upgrade** (or Fluid/RF Upgrade) into the Exporter to start transferring.

> [!NOTE]
> For a complete guide covering all upgrade tiers, rates, and filtering options, check out the **[Full Tutorial](file:///d:/projekt/BBL%20Router/tutorial.md)**.

---

## 🛠️ Visualizing Connections

Hold any **Wrench** (any item tagged under `c:tools/wrench`) in your main hand to see active connection beams in the world:

- 🔴 **Red Beam**: RF / Energy
- ⚪ **Grey Beam**: Items
- 🔵 **Blue Beam**: Fluids
- 🟣 **Purple Beam**: Chemicals *(Mekanism)*
- 🩵 **Light Blue Beam**: Souls *(Industrial Foregoing)*
- 🩷 **Pink Beam**: Source *(Ars Nouveau)*
- 🟢 **Cyan Beam**: Pressure *(PneumaticCraft)*

---

## ⚙️ Configuration

Transfer rates, speed operation delays, and default capacities can be customized in `.minecraft/config/routers-startup.toml`.

---

## 📜 License

Created by **BenBenLaw**. Free to use in modpacks!
