package com.benbenlaw.routers.data;

import com.benbenlaw.routers.Routers;
import com.benbenlaw.routers.block.RoutersBlocks;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.packs.VanillaBlockLoot;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class RoutersLootTableProvider extends VanillaBlockLoot {

    public RoutersLootTableProvider(HolderLookup.Provider p_344962_) {
        super(p_344962_);
    }

    @Override
    protected void generate() {

        this.dropSelf(RoutersBlocks.IMPORTER_BLOCK.get());
        this.dropSelf(RoutersBlocks.EXPORTER_BLOCK.get());
        this.dropSelf(RoutersBlocks.ROUTER_BLOCK.get());
    }

    @Override
    protected void add(@NotNull Block block, @NotNull LootTable.Builder table) {
        //Overwrite the core register method to add to our list of known blocks
        super.add(block, table);
        knownBlocks.add(block);
    }
    private final Set<Block> knownBlocks = new ReferenceOpenHashSet<>();

    @NotNull
    @Override
    protected Iterable<Block> getKnownBlocks() {
        return knownBlocks;
    }

}
