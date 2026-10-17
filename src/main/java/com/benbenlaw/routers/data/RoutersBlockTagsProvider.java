package com.benbenlaw.routers.data;

import com.benbenlaw.routers.Routers;
import com.benbenlaw.routers.block.RoutersBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class RoutersBlockTagsProvider extends BlockTagsProvider {

    RoutersBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, Routers.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.@NotNull Provider provider) {


        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(RoutersBlocks.EXPORTER_BLOCK.get())
                .add(RoutersBlocks.IMPORTER_BLOCK.get())
                .add(RoutersBlocks.ROUTER_BLOCK.get())
        ;

    }


    @Override
    public @NotNull String getName() {
        return Routers.MOD_ID + " Block Tags";
    }
}
