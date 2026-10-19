package com.benbenlaw.routers.data;

import com.benbenlaw.routers.Routers;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class RoutersLangProvider extends LanguageProvider {

    public RoutersLangProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, Routers.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {

        //Creative Tab
        add("itemGroup.routers", "Routers");

        //Items
        addItemTranslation("router_connector", "Connector");
        addItemTranslation("rf_upgrade_1", "RF Upgrade I");
        addItemTranslation("rf_upgrade_2", "RF Upgrade II");
        addItemTranslation("rf_upgrade_3", "RF Upgrade III");
        addItemTranslation("rf_upgrade_4", "RF Upgrade IV");

        addItemTranslation("item_upgrade_1", "Item Upgrade I");
        addItemTranslation("item_upgrade_2", "Item Upgrade II");
        addItemTranslation("item_upgrade_3", "Item Upgrade III");
        addItemTranslation("item_upgrade_4", "Item Upgrade IV");

        addItemTranslation("fluid_upgrade_1", "Fluid Upgrade I");
        addItemTranslation("fluid_upgrade_2", "Fluid Upgrade II");
        addItemTranslation("fluid_upgrade_3", "Fluid Upgrade III");
        addItemTranslation("fluid_upgrade_4", "Fluid Upgrade IV");

        addItemTranslation("chemical_upgrade_1", "Chemical Upgrade I");
        addItemTranslation("chemical_upgrade_2", "Chemical Upgrade II");
        addItemTranslation("chemical_upgrade_3", "Chemical Upgrade III");
        addItemTranslation("chemical_upgrade_4", "Chemical Upgrade IV");

        addItemTranslation("speed_upgrade_1", "Speed Upgrade I");
        addItemTranslation("speed_upgrade_2", "Speed Upgrade II");
        addItemTranslation("speed_upgrade_3", "Speed Upgrade III");
        addItemTranslation("speed_upgrade_4", "Speed Upgrade IV");

        addItemTranslation("source_upgrade_1", "Source Upgrade I");
        addItemTranslation("source_upgrade_2", "Source Upgrade II");
        addItemTranslation("source_upgrade_3", "Source Upgrade III");
        addItemTranslation("source_upgrade_4", "Source Upgrade IV");


        addItemTranslation("soul_upgrade", "Soul Upgrade");

        addItemTranslation("round_robin_upgrade", "Round Robin Upgrade");
        addItemTranslation("mod_filter", "Mod Filter");
        addItemTranslation("tag_filter", "Tag Filter");
        addItemTranslation("dimensional_upgrade", "Dimensional Upgrade");

        addItemTranslation("pressure_upgrade", "Basic Pressure Upgrade");
        addItemTranslation("reinforced_pressure_upgrade", "Reinforced Pressure Upgrade");
        addItemTranslation("advanced_pressure_upgrade", "Advanced Pressure Upgrade");

        addItemTranslation("heat_upgrade_pc", "Heat Upgrade (PneumaticCraft)");



        //Blocks
        addBlockTranslation("importer_block", "Importer");
        addBlockTranslation("exporter_block", "Exporter");
        addBlockTranslation("router_block", "Router");

        //Messages
        addMessageTranslation("exporter.remove_importer", "Removed Importer from Exporter");
        addMessageTranslation("exporter.add_importer", "Added Importer to Exporter");
        addMessageTranslation("importer.removed_pos", "Removed Importer position");
        addMessageTranslation("importer.added_pos", "Added Importer position");

        //Tooltips
        addTooltipsTranslation("hold_shift", "Press SHIFT for more info");
        addTooltipsTranslation("rf_upgrade", "Allows Extraction of RF from the Exporter at %s RF/Per Operation");
        addTooltipsTranslation("item_upgrade", "Allows Extraction of Items from the Exporter at %s Items/Per Operation");
        addTooltipsTranslation("fluid_upgrade", "Allows Extraction of Fluids from the Exporter at %s MB/Per Operation");
        addTooltipsTranslation("chemical_upgrade", "Allows Extraction of Mekanism Chemicals from the Exporter at %s mB/Per Operation");
        addTooltipsTranslation("speed_upgrade", "Allows Extraction of from the Exporter every %s Ticks");
        addTooltipsTranslation("source_upgrade", "Allows Extraction of Ars Nouveau Source from the Exporter at %s Per Operation");
        addTooltipsTranslation("round_robin_upgrade", "Extractor distributes in order evenly between multiple Importers");
        addTooltipsTranslation("soul_upgrade", "Allows Extraction of Industrial Forgoing Souls Soul from the Exporter at 10 Per Operation, Importer must be connected to a Soul Surge");
        addTooltipsTranslation("tag_filter_empty", "Right Click to open, allows filtering by Mod");
        addTooltipsTranslation("mod_filter_empty", "Right Click to open, allows filtering by Tag");
        addTooltipsTranslation("tag_filter", "Tag: %s");
        addTooltipsTranslation("mod_filter", "Mod: %s");
        addTooltipsTranslation("dimensional_upgrade", "Allows the Exporter to extract to Importers in other Dimensions");
        addTooltipsTranslation("pressure_upgrade", "Allows Extraction of Pneumaticcraft Air from the Exporter at %s Air/Per Operation");



        addTooltipsTranslation("router_connector", "Right Click on an Importer to set the position then Right Click on an Exporter to link them. Right Click to unlink.");
        addTooltipsTranslation("exporter", "Does nothing on its own, used to extract from the connected inventory to a connected Importer(s). Use Upgrades to allow different types of extraction.");
        addTooltipsTranslation("importer", "Does nothing on its own, used to insert into the connected inventory from a connected Exporter.");
        addTooltipsTranslation("router", "Exporter and Importer in one block: extracts from and inserts into the connected inventory. Sneak + Right Click with a Connector to set it as an Importer.");
        addTooltipsTranslation("exporter_filter_slots", "Empty Item/Fluid Filter");
        addTooltipsTranslation("exporter_upgrades_slots", "Upgrade Slot");

        add("screen.routers.config_screen", "Set Filter");
        add("screen.routers.router.export_tab", "Export");
        add("screen.routers.router.import_tab", "Import");

    }

    private void addItemTranslation(String name, String translation) {
        add("item." + Routers.MOD_ID + "." + name, translation);
    }

    private void addBlockTranslation(String name, String translation) {
        add("block." + Routers.MOD_ID + "." + name, translation);
    }

    private void addMessageTranslation(String name, String translation) {
        add("message." + Routers.MOD_ID + "." + name, translation);
    }

    private void addTooltipsTranslation(String name, String translation) {
        add("tooltip." + Routers.MOD_ID + "." + name, translation);
    }
}

