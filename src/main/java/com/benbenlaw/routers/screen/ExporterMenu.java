package com.benbenlaw.routers.screen;

import com.benbenlaw.routers.Routers;
import com.benbenlaw.routers.block.RoutersBlocks;
import com.benbenlaw.routers.block.entity.ExporterBlockEntity;
import com.benbenlaw.routers.block.entity.MekanismCompat;
import com.benbenlaw.routers.screen.util.GhostSlot;
import com.mojang.datafixers.util.Pair;
import mekanism.api.chemical.ChemicalStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class ExporterMenu extends AbstractContainerMenu {

    protected ExporterBlockEntity blockEntity;
    protected Level level;
    protected ContainerData data;
    protected Player player;
    protected BlockPos blockPos;
    public SimpleContainer filterInventory;

    public ExporterMenu(int containerID, Inventory inventory, FriendlyByteBuf extraData) {
        this(containerID, inventory, extraData.readBlockPos(), new SimpleContainerData(2));
    }

    public ExporterMenu(int containerID, Inventory inventory, BlockPos blockPos, ContainerData data) {
        super(RoutersMenuTypes.EXPORTER_MENU.get(), containerID);
        this.player = inventory.player;
        this.blockPos = blockPos;
        this.level = inventory.player.level();
        this.data = data;
        this.blockEntity = (ExporterBlockEntity) this.level.getBlockEntity(blockPos);

        this.filterInventory = new SimpleContainer(Objects.requireNonNull(blockEntity).getFilters().size()) {
            @Override
            public void setChanged() {
                super.setChanged();
                blockEntity.setChanged();
            }
        };

        for (int i = 0; i < blockEntity.getFilters().size(); i++) {
            this.filterInventory.setItem(i, blockEntity.getFilters().get(i));
        }

        // Ghost slots for filters
        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 9; col++) {
                GhostSlot slot = getGhostSlot(col, row);
                this.addSlot(slot);
            }
        }

        //Upgrade Slots (real slots from block entity)
        for (int col = 0; col < 9; col++) {
            this.addSlot(new SlotItemHandler(blockEntity.getItemStackHandler(), col, 8 + col * 18, 57 ){
                @Override
                public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
                    return Pair.of(InventoryMenu.BLOCK_ATLAS, ResourceLocation.fromNamespaceAndPath(Routers.MOD_ID, "item/gui/upgrade_slot"));
                }
            });
        }

        // Add player inventory and hotbar
        addPlayerInventory(inventory);
        addPlayerHotbar(inventory);
    }

    public void setFilterItem(int slotIndex, ItemStack stack) {
        if (slotIndex < 0 || slotIndex >= filterInventory.getContainerSize()) return;

        filterInventory.setItem(slotIndex, stack);
        blockEntity.getFilters().set(slotIndex, stack);
        blockEntity.getFluidFilters().set(slotIndex, FluidStack.EMPTY);

        if (ModList.get().isLoaded("mekanism")) {
            @SuppressWarnings("unchecked")
            NonNullList<Object> chemicals = (NonNullList<Object>) blockEntity.getChemicalFilters();
            if (slotIndex < chemicals.size()) {
                Object emptyChemical = MekanismCompat.EMPTY_CHEMICAL != null
                        ? MekanismCompat.EMPTY_CHEMICAL
                        : MekanismCompat.createChemicalStack();
                chemicals.set(slotIndex, emptyChemical);
            }
        }

        if (slotIndex < slots.size() && slots.get(slotIndex) instanceof com.benbenlaw.routers.screen.util.GhostSlot ghostSlot) {
            ghostSlot.set(stack);
            ghostSlot.setFluid(FluidStack.EMPTY);
        }

        blockEntity.setChanged();
    }

    private @NotNull GhostSlot getGhostSlot(int col, int row) {
        int index = col + row * 9;
        GhostSlot slot = new GhostSlot(filterInventory, index, 8 + col * 18, 18 + row * 18);

        if (blockEntity.getFluidFilters().size() > index) {
            FluidStack fluid = blockEntity.getFluidFilters().get(index);
            if (!fluid.isEmpty()) {
                slot.setFluid(fluid);
            }
        }

        if (blockEntity.getFilters().size() > index) {
            ItemStack item = blockEntity.getFilters().get(index);
            if (!item.isEmpty()) {
                slot.setItem(item);
            }
        }
        return slot;
    }

    public BlockPos getBlockPos() {
        return blockPos;
    }

    public void updateFluids(List<FluidStack> fluids) {
        for (int i = 0; i < slots.size(); i++) {
            if (slots.get(i) instanceof GhostSlot ghostSlot) {
                FluidStack fluid = i < fluids.size() ? fluids.get(i) : FluidStack.EMPTY;
                ghostSlot.setFluid(fluid);
            }
        }
    }

    public void updateChemicals(List<?> chemicals) {
        if (!ModList.get().isLoaded("mekanism")) return;
        for (int i = 0; i < slots.size(); i++) {
            if (slots.get(i) instanceof GhostSlot ghostSlot) {
                Object chemical = i < chemicals.size() ? chemicals.get(i) : MekanismCompat.EMPTY_CHEMICAL;
                ghostSlot.setChemical(chemical);
            }
        }
    }


    @Override
    public void removed(Player player) {
        super.removed(player);
        for (int i = 0; i < blockEntity.getFilters().size(); i++) {
            blockEntity.getFilters().set(i, filterInventory.getItem(i));
        }
        blockEntity.setChanged();
    }

    @Override
    public void clicked(int slotId, int dragType, ClickType clickType, Player player) {
        if (slotId < 0 || slotId >= slots.size()) {
            super.clicked(slotId, dragType, clickType, player);
            return;
        }
        Slot slot = slots.get(slotId);
        if (slot instanceof GhostSlot ghostSlot) {
            ItemStack carried = player.containerMenu.getCarried();
            var fluid = FluidUtil.getFluidContained(carried);

            // Handle fluids
            if (fluid.isPresent()) {
                blockEntity.getFilters().set(slotId, ItemStack.EMPTY);
                blockEntity.getFluidFilters().set(slotId, fluid.get());
                ghostSlot.set(ItemStack.EMPTY);
                ghostSlot.setFluid(fluid.get());
                if (ModList.get().isLoaded("mekanism")) {
                    @SuppressWarnings("unchecked")
                    NonNullList<Object> chemicals = (NonNullList<Object>) blockEntity.getChemicalFilters();
                    Object emptyChemical = MekanismCompat.EMPTY_CHEMICAL != null ? MekanismCompat.EMPTY_CHEMICAL : MekanismCompat.createChemicalStack();
                    chemicals.set(slotId, emptyChemical);
                    ghostSlot.setChemical(emptyChemical);
                }
            }
            // Handle items
            else if (!carried.isEmpty()) {
                blockEntity.getFilters().set(slotId, carried.copyWithCount(1));
                blockEntity.getFluidFilters().set(slotId, FluidStack.EMPTY);
                ghostSlot.set(carried.copyWithCount(1));
                ghostSlot.setFluid(FluidStack.EMPTY);
                if (ModList.get().isLoaded("mekanism")) {
                    @SuppressWarnings("unchecked")
                    NonNullList<Object> chemicals = (NonNullList<Object>) blockEntity.getChemicalFilters();
                    Object emptyChemical = MekanismCompat.EMPTY_CHEMICAL != null ? MekanismCompat.EMPTY_CHEMICAL : MekanismCompat.createChemicalStack();
                    chemicals.set(slotId, emptyChemical);
                    ghostSlot.setChemical(emptyChemical);
                }
            }
            // Handle clearing
            else {
                blockEntity.getFilters().set(slotId, ItemStack.EMPTY);
                blockEntity.getFluidFilters().set(slotId, FluidStack.EMPTY);
                ghostSlot.set(ItemStack.EMPTY);
                ghostSlot.setFluid(FluidStack.EMPTY);
                if (ModList.get().isLoaded("mekanism")) {
                    @SuppressWarnings("unchecked")
                    NonNullList<Object> chemicals = (NonNullList<Object>) blockEntity.getChemicalFilters();
                    Object emptyChemical = MekanismCompat.EMPTY_CHEMICAL != null ? MekanismCompat.EMPTY_CHEMICAL : MekanismCompat.createChemicalStack();
                    chemicals.set(slotId, emptyChemical);
                    ghostSlot.setChemical(emptyChemical);
                }
            }

            blockEntity.setChanged();
        }
        super.clicked(slotId, dragType, clickType, player);
    }


    private static final int HOTBAR_SLOT_COUNT = 9;
    private static final int PLAYER_INVENTORY_ROW_COUNT = 3;
    private static final int PLAYER_INVENTORY_COLUMN_COUNT = 9;
    private static final int PLAYER_INVENTORY_SLOT_COUNT = PLAYER_INVENTORY_COLUMN_COUNT * PLAYER_INVENTORY_ROW_COUNT;
    private static final int VANILLA_SLOT_COUNT = HOTBAR_SLOT_COUNT + PLAYER_INVENTORY_SLOT_COUNT;
    private static final int VANILLA_FIRST_SLOT_INDEX = 0;
    private static final int TE_INVENTORY_FIRST_SLOT_INDEX = VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT;

    private static final int TE_INVENTORY_SLOT_COUNT = 45;  // must be the number of slots you have!

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        Slot sourceSlot = slots.get(index);
        if (sourceSlot == null || !sourceSlot.hasItem()) return ItemStack.EMPTY;

        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack copyOfSourceStack = sourceStack.copy();

        int ghostSlotCount = 18; // 2 rows × 9 columns
        int upgradeSlotCount = 9;

        int upgradeFirst = ghostSlotCount;                 // 18
        int upgradeLast = upgradeFirst + upgradeSlotCount; // 27
        int playerFirst = upgradeLast;                     // 27
        int playerLast = playerFirst + VANILLA_SLOT_COUNT; // 27 + 36 = 63

        if (index >= playerFirst && index < playerLast) {
            // Shift-clicked from player inventory → move into upgrade slots
            if (!moveItemStackTo(sourceStack, upgradeFirst, upgradeLast, false)) {
                return ItemStack.EMPTY;
            }
        } else if (index >= upgradeFirst && index < upgradeLast) {
            // Shift-clicked from upgrade slots → move into player inventory
            if (!moveItemStackTo(sourceStack, playerFirst, playerLast, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            // Shift-clicked a ghost slot → ignore
            return ItemStack.EMPTY;
        }

        if (sourceStack.isEmpty()) {
            sourceSlot.set(ItemStack.EMPTY);
        } else {
            sourceSlot.setChanged();
        }

        sourceSlot.onTake(playerIn, sourceStack);
        return copyOfSourceStack;
    }



    @Override
    public boolean stillValid(@NotNull Player player) {
        return stillValid(ContainerLevelAccess.create(player.level(), blockPos),
                player, RoutersBlocks.EXPORTER_BLOCK.get());
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 8 + l * 18, 90 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 148));
        }
    }
}
