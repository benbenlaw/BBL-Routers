package com.benbenlaw.routers.block.entity;

import com.benbenlaw.routers.block.ImporterBlock;
import com.benbenlaw.routers.integration.RoutersCapabilities;
import com.benbenlaw.routers.screen.ImporterMenu;
import com.benbenlaw.routers.screen.util.FluidContainerHelper;
import com.buuz135.industrialforegoingsouls.block.tile.NetworkBlockEntity;
import com.buuz135.industrialforegoingsouls.block_network.SoulNetwork;
import com.buuz135.industrialforegoingsouls.capabilities.ISoulHandler;
import com.buuz135.industrialforegoingsouls.capabilities.SoulCapabilities;
import com.hollingsworth.arsnouveau.api.source.ISourceCap;
import com.hollingsworth.arsnouveau.setup.registry.CapabilityRegistry;
import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.api.tileentity.IAirHandlerMachine;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static me.desht.pneumaticcraft.api.PNCCapabilities.AIR_HANDLER_MACHINE;
import static me.desht.pneumaticcraft.api.PNCCapabilities.HEAT_EXCHANGER_BLOCK;

public class ImporterBlockEntity extends BlockEntity implements MenuProvider {

    private BlockPos extractorPos;
    private IItemHandler itemHandler;
    private IFluidHandler fluidHandler;
    private IEnergyStorage energyStorage;
    private IChemicalHandler chemicalHandler;
    private ISourceCap sourceStorage;
    private ISoulHandler soulHandler;
    private SoulNetwork soulNetwork;
    private IAirHandlerMachine pressureHandler;
    private IHeatExchangerLogic heatHandlerPC;
    public final ContainerData data;
    private String dimension = "";
    private final NonNullList<ItemStack> filters = NonNullList.withSize(18, ItemStack.EMPTY);
    private final NonNullList<FluidStack> fluidFilters = NonNullList.withSize(18, FluidStack.EMPTY);
    private final NonNullList<?> chemicalFilters;

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.routers.importer_block");
    }

    @Override
    public AbstractContainerMenu createMenu(int container, @NotNull Inventory inventory, @NotNull Player player) {
        return new ImporterMenu(container, inventory, this.getBlockPos(), data);
    }

    public ImporterBlockEntity(BlockPos pos, BlockState state) {
        this(RoutersBlockEntities.IMPORTER_BLOCK_ENTITY.get(), pos, state);
    }

    protected ImporterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);

        if (ModList.get().isLoaded("mekanism")) {
            chemicalFilters = MekanismCompat.createChemicalFilters();
        } else {
            chemicalFilters = NonNullList.withSize(18, ItemStack.EMPTY);
        }

        this.data = new ContainerData() {
            private final int[] values = new int[2];

            @Override
            public int get(int index) {
                return values[index];
            }

            @Override
            public void set(int index, int value) {
                values[index] = value;
            }

            @Override
            public int getCount() {
                return values.length;
            }
        };
    }

    public String getDimension() {
        return dimension;
    }

    public String setDimension(String dimension) {
        return this.dimension = dimension;
    }


    public NonNullList<ItemStack> getFilters() {
        return filters;
    }

    public NonNullList<FluidStack> getFluidFilters() {
        return fluidFilters;
    }

    public NonNullList<?> getChemicalFilters() {
        return chemicalFilters;
    }

    public void tick() {
        if (level == null || level.isClientSide) return;

        Direction facing = this.getBlockState().getValue(ImporterBlock.FACING);
        BlockPos targetPos = worldPosition.relative(facing);
        assert level != null;
        BlockEntity targetBlockEntity = level.getBlockEntity(targetPos);
        Direction inputDirection = facing.getOpposite();

        if (level.getGameTime() % 20 != 0) return;

        if (dimension == null || dimension.isEmpty()) {
            dimension = level.dimension().location().toString();
            setChanged();
        }

        if (targetBlockEntity != null) {
            IItemHandler itemHandler = Capabilities.ItemHandler.BLOCK.getCapability(level, targetPos, level.getBlockState(targetPos), targetBlockEntity, inputDirection);
            if (itemHandler != null) {
                setItemHandler(itemHandler);
            }
            IFluidHandler fluidHandler = Capabilities.FluidHandler.BLOCK.getCapability(level, targetPos, level.getBlockState(targetPos), targetBlockEntity, inputDirection);
            if (fluidHandler != null) {
                setFluidHandler(fluidHandler);
            }
            IEnergyStorage energyStorage = Capabilities.EnergyStorage.BLOCK.getCapability(level, targetPos, level.getBlockState(targetPos), targetBlockEntity, inputDirection);
            if (energyStorage != null) {
                setEnergyStorage(energyStorage);
            }

            if (ModList.get().isLoaded("mekanism")) {
                IChemicalHandler chemicalHandler = RoutersCapabilities.CHEMICAL_HANDLER.getCapability(level, targetPos, level.getBlockState(targetPos), targetBlockEntity, inputDirection);
                if (chemicalHandler != null) {
                    setChemicalHandler(chemicalHandler);
                }
            }

            if (ModList.get().isLoaded("ars_nouveau")) {
                ISourceCap sourceHandler = CapabilityRegistry.SOURCE_CAPABILITY.getCapability(level, targetPos, level.getBlockState(targetPos), targetBlockEntity, inputDirection);
                if (sourceHandler != null) {
                    setSourceHandler(sourceHandler);
                }
            }

            if (ModList.get().isLoaded("industrialforegoingsouls")) {
                ISoulHandler soulHandler = SoulCapabilities.BLOCK.getCapability(level, targetPos, level.getBlockState(targetPos), targetBlockEntity, inputDirection);
                if (soulHandler != null) {
                    setSoulHandler(soulHandler);
                }
                if (targetBlockEntity instanceof NetworkBlockEntity<?> networkBlockEntity && networkBlockEntity.getNetwork() instanceof SoulNetwork soulNetwork) {
                    setSoulNetwork(soulNetwork);
                }
            }

            if (ModList.get().isLoaded("pneumaticcraft")) {
                IAirHandlerMachine pressureHandler = level.getCapability(AIR_HANDLER_MACHINE, targetBlockEntity.getBlockPos(), inputDirection);
                if (pressureHandler != null) {
                    setPressureHandler(pressureHandler);
                }

                IHeatExchangerLogic heatHandler = level.getCapability(HEAT_EXCHANGER_BLOCK, targetBlockEntity.getBlockPos(), inputDirection);
                if (heatHandler != null) {
                    setHeatHandlerPC(heatHandler);
                }
            }
        }
    }

    public void setItemHandler(IItemHandler handler) {
        this.itemHandler = handler;
    }

    public IItemHandler getTargetHandler() {
        return itemHandler;
    }

    public void setFluidHandler(IFluidHandler handler) {
        this.fluidHandler = handler;
    }

    public IFluidHandler getFluidHandler() {
        return fluidHandler;
    }

    public void setEnergyStorage(IEnergyStorage storage) {
        this.energyStorage = storage;
    }

    public IEnergyStorage getEnergyStorage() {
        return energyStorage;
    }

    public IChemicalHandler getChemicalHandler() {
        return chemicalHandler;
    }

    public void setChemicalHandler(IChemicalHandler handler) {
        this.chemicalHandler = handler;
    }

    public ISourceCap getSourceHandler() {
        return sourceStorage;
    }

    public void setSourceHandler(ISourceCap handler) {
        this.sourceStorage = handler;
    }

    public ISoulHandler getSoulHandler() {
        return soulHandler;
    }

    public void setSoulHandler(ISoulHandler handler) {
        this.soulHandler = handler;
    }

    public SoulNetwork getSoulNetwork() {
        return soulNetwork;
    }

    public void setSoulNetwork(SoulNetwork soulNetwork) {
        this.soulNetwork = soulNetwork;
    }

    public IAirHandlerMachine getPressureHandler() {
        return pressureHandler;
    }

    public void setPressureHandler(IAirHandlerMachine handler) {
        this.pressureHandler = handler;
    }

    public IHeatExchangerLogic getHeatHandlerPC() {
        return heatHandlerPC;
    }

    public void setHeatHandlerPC(IHeatExchangerLogic handler) {
        this.heatHandlerPC = handler;
    }



    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider p_323910_) {
        return saveWithoutMetadata(p_323910_);
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider lookupProvider) {
        loadAdditional(tag, lookupProvider);
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag compoundTag, HolderLookup.@NotNull Provider provider) {
        super.saveAdditional(compoundTag, provider);

        ContainerHelper.saveAllItems(compoundTag, this.filters, provider);
        FluidContainerHelper.saveAllFluids(compoundTag, this.fluidFilters, true, provider);

        if (ModList.get().isLoaded("mekanism")) {
            MekanismCompat.saveChemicalFilters(compoundTag, (NonNullList<ChemicalStack>) chemicalFilters, provider);
        }

        if (extractorPos != null) {
            compoundTag.putInt("extractorX", extractorPos.getX());
            compoundTag.putInt("extractorY", extractorPos.getY());
            compoundTag.putInt("extractorZ", extractorPos.getZ());
        }

        compoundTag.putString("dimension", dimension);

    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag compoundTag, HolderLookup.@NotNull Provider provider) {
        super.loadAdditional(compoundTag, provider);

        ContainerHelper.loadAllItems(compoundTag, this.filters, provider);
        FluidContainerHelper.loadAllFluids(compoundTag, this.fluidFilters, provider);

        if (ModList.get().isLoaded("mekanism")) {
            MekanismCompat.loadChemicalFilters(compoundTag, (NonNullList<ChemicalStack>) chemicalFilters, provider);
        }

        if (compoundTag.contains("extractorX") && compoundTag.contains("extractorY") && compoundTag.contains("extractorZ")) {
            extractorPos = new BlockPos(compoundTag.getInt("extractorX"), compoundTag.getInt("extractorY"), compoundTag.getInt("extractorZ"));
        } else {
            extractorPos = null;
        }

        dimension = compoundTag.getString("dimension");

    }
}
