package com.benbenlaw.routers.gametest;

import com.benbenlaw.routers.Routers;
import com.benbenlaw.routers.block.RoutersBlocks;
import com.benbenlaw.routers.block.entity.ExporterBlockEntity;
import com.benbenlaw.routers.config.StartupConfig;
import com.benbenlaw.routers.item.RoutersItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.transfer.item.ItemResource;

import java.util.function.Consumer;

public class RoutersGameTests {

    public static final DeferredRegister<Consumer<GameTestHelper>> TEST_FUNCTIONS =
            DeferredRegister.create(Registries.TEST_FUNCTION, Routers.MOD_ID);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> EXPORTER_ITEM_FILTER =
            TEST_FUNCTIONS.register("exporter_item_filter", () -> RoutersGameTests::exporterItemFilterOnlyLetsFilteredItemThrough);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> EXPORTER_FINDS_SCATTERED_ITEM =
            TEST_FUNCTIONS.register("exporter_finds_scattered_item", () -> RoutersGameTests::exporterFindsItemPastFirstScanWindow);

    public static void registerTests(RegisterGameTestsEvent event) {
        Holder<TestEnvironmentDefinition<?>> environment = event.registerEnvironment(Routers.identifier("default"));

        TestData<Holder<TestEnvironmentDefinition<?>>> testData = new TestData<>(
                environment,
                Identifier.withDefaultNamespace("empty"),
                120,
                1,
                true,
                Rotation.NONE,
                false,
                1,
                1,
                false,
                8
        );

        event.registerTest(Routers.identifier("exporter_item_filter"),
                new FunctionGameTestInstance(EXPORTER_ITEM_FILTER.getKey(), testData));

        TestData<Holder<TestEnvironmentDefinition<?>>> longTestData = new TestData<>(
                environment,
                Identifier.withDefaultNamespace("empty"),
                1000,
                1,
                true,
                Rotation.NONE,
                false,
                1,
                1,
                false,
                8
        );

        event.registerTest(Routers.identifier("exporter_finds_scattered_item"),
                new FunctionGameTestInstance(EXPORTER_FINDS_SCATTERED_ITEM.getKey(), longTestData));
    }

    private static void exporterItemFilterOnlyLetsFilteredItemThrough(GameTestHelper helper) {
        BlockPos exporterPos = new BlockPos(1, 1, 1);
        BlockPos sourceChestPos = new BlockPos(1, 1, 2);
        BlockPos importerPos = new BlockPos(5, 1, 1);
        BlockPos destChestPos = new BlockPos(5, 1, 2);

        helper.setBlock(exporterPos, RoutersBlocks.EXPORTER.get(), Direction.SOUTH);
        helper.setBlock(sourceChestPos, Blocks.CHEST);
        helper.setBlock(importerPos, RoutersBlocks.IMPORTER.get(), Direction.SOUTH);
        helper.setBlock(destChestPos, Blocks.CHEST);

        ChestBlockEntity sourceChest = helper.getBlockEntity(sourceChestPos, ChestBlockEntity.class);
        sourceChest.setItem(0, new ItemStack(Items.DIAMOND, 5));
        sourceChest.setItem(1, new ItemStack(Items.DIRT, 5));

        ExporterBlockEntity exporter = helper.getBlockEntity(exporterPos, ExporterBlockEntity.class);
        exporter.toggleImporterPosition(GlobalPos.of(helper.getLevel().dimension(), helper.absolutePos(importerPos)));

        exporter.getUpgradeItemHandler().set(0, ItemResource.of(RoutersItems.ITEM_UPGRADE_1.get()), 1);
        exporter.getFilterItemHandler().set(0, ItemResource.of(Items.DIAMOND), 1);

        helper.startSequence()
                .thenWaitUntil(() -> helper.assertContainerContains(destChestPos, Items.DIAMOND))
                .thenExecute(() -> {
                    ChestBlockEntity dest = helper.getBlockEntity(destChestPos, ChestBlockEntity.class);
                    boolean hasDirt = false;
                    for (int i = 0; i < dest.getContainerSize(); i++) {
                        if (dest.getItem(i).is(Items.DIRT)) {
                            hasDirt = true;
                        }
                    }
                    helper.assertTrue(!hasDirt, "Dirt should have been blocked by the exporter's item filter");
                })
                .thenSucceed();
    }

    private static void exporterFindsItemPastFirstScanWindow(GameTestHelper helper) {
        int originalScanSize = StartupConfig.maxInventoryScanPerOperation.get();
        StartupConfig.maxInventoryScanPerOperation.set(3);
        helper.runBeforeTestEnd(() -> StartupConfig.maxInventoryScanPerOperation.set(originalScanSize));

        BlockPos exporterPos = new BlockPos(1, 1, 1);
        BlockPos sourceChestPos = new BlockPos(1, 1, 2);
        BlockPos importerPos = new BlockPos(5, 1, 1);
        BlockPos destChestPos = new BlockPos(5, 1, 2);

        helper.setBlock(exporterPos, RoutersBlocks.EXPORTER.get(), Direction.SOUTH);
        helper.setBlock(sourceChestPos, Blocks.CHEST);
        helper.setBlock(importerPos, RoutersBlocks.IMPORTER.get(), Direction.SOUTH);
        helper.setBlock(destChestPos, Blocks.CHEST);

        ChestBlockEntity sourceChest = helper.getBlockEntity(sourceChestPos, ChestBlockEntity.class);
        sourceChest.setItem(26, new ItemStack(Items.DIAMOND, 1));

        ExporterBlockEntity exporter = helper.getBlockEntity(exporterPos, ExporterBlockEntity.class);
        exporter.toggleImporterPosition(GlobalPos.of(helper.getLevel().dimension(), helper.absolutePos(importerPos)));
        exporter.getUpgradeItemHandler().set(0, ItemResource.of(RoutersItems.ITEM_UPGRADE_1.get()), 1);

        helper.startSequence()
                .thenWaitUntil(() -> helper.assertContainerContains(destChestPos, Items.DIAMOND))
                .thenExecute(() -> StartupConfig.maxInventoryScanPerOperation.set(originalScanSize))
                .thenSucceed();
    }
}
