package com.benbenlaw.routers.block;

import com.benbenlaw.routers.Routers;
import com.benbenlaw.routers.item.RoutersItems;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class RoutersBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Routers.MOD_ID);

    public static final DeferredBlock<ImporterBlock> IMPORTER_BLOCK = registerBlock("importer_block",
            () -> new ImporterBlock(Block.Properties.of().strength(2.0f).noOcclusion()));

    public static final DeferredBlock<ExporterBlock> EXPORTER_BLOCK = registerBlock("exporter_block",
            () -> new ExporterBlock(Block.Properties.of().strength(2.0f).noOcclusion()));

    public static final DeferredBlock<RouterBlock> ROUTER_BLOCK = registerBlock("router_block",
            () -> new RouterBlock(Block.Properties.of().strength(2.0f).noOcclusion()));


    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block) {
        DeferredBlock<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block) {
        RoutersItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }
}
