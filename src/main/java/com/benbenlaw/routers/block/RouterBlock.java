package com.benbenlaw.routers.block;

import com.benbenlaw.routers.block.entity.RouterBlockEntity;
import com.benbenlaw.routers.block.entity.RoutersBlockEntities;
import com.benbenlaw.routers.item.RoutersDataComponents;
import com.benbenlaw.routers.item.RoutersItems;
import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Exporter and Importer in one block. Shape and facing come from the Exporter.
 */
public class RouterBlock extends ExporterBlock {

    public static final MapCodec<RouterBlock> CODEC = simpleCodec(RouterBlock::new);

    public RouterBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        if (pState.getBlock() != pNewState.getBlock() && pLevel.getBlockEntity(pPos) instanceof RouterBlockEntity router) {
            router.getExporter().drops();
        }
        super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (level.getBlockEntity(pos) instanceof RouterBlockEntity entity) {
            entity.setFluidHandler(null);
            entity.setItemHandler(null);
            entity.setEnergyStorage(null);
            if (ModList.get().isLoaded("mekanism")) {
                entity.setChemicalHandler(null);
            }
            if (ModList.get().isLoaded("ars_nouveau")) {
                entity.setSourceHandler(null);
            }
            if (ModList.get().isLoaded("industrialforegoingsouls")) {
                entity.setSoulNetwork(null);
                entity.setSoulHandler(null);
            }
            if (ModList.get().isLoaded("pneumaticcraft")) {
                entity.setHeatHandlerPC(null);
                entity.setPressureHandler(null);
            }
        }
    }

    @Override
    protected @NotNull ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos blockPos, Player player, InteractionHand hand, BlockHitResult result) {

        if (level.isClientSide()) return ItemInteractionResult.SUCCESS;

        if (!(level.getBlockEntity(blockPos) instanceof RouterBlockEntity router)) return ItemInteractionResult.SUCCESS;

        if (stack.is(RoutersItems.ROUTER_CONNECTOR)) {
            BlockPos savedPos = stack.get(RoutersDataComponents.IMPORTER_POSITION);

            if (savedPos == null) {
                // Nothing saved yet: this router becomes the importer to link to
                stack.set(RoutersDataComponents.IMPORTER_POSITION, blockPos);
                player.displayClientMessage(Component.translatable("message.routers.importer.added_pos"), true);
            } else if (savedPos.equals(blockPos)) {
                // Clicking the saved router again clears it, same as the importer (and avoids self links)
                stack.remove(RoutersDataComponents.IMPORTER_POSITION);
                player.displayClientMessage(Component.translatable("message.routers.importer.removed_pos"), true);
            } else {
                // Act as the exporter for the saved importer
                router.getExporter().toggleImporter(savedPos, player);
            }
        } else if (player instanceof ServerPlayer serverPlayer) {
            router.openMenu(serverPlayer, false);
        }

        return ItemInteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> components, TooltipFlag flag) {
        if (Screen.hasShiftDown()) {
            components.add(Component.translatable("tooltip.routers.router").withStyle(ChatFormatting.YELLOW));
        } else {
            components.add(Component.translatable("tooltip.routers.hold_shift").withStyle(ChatFormatting.YELLOW));
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new RouterBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState blockState, @NotNull BlockEntityType<T> blockEntityType) {
        return createTickerHelper(blockEntityType, RoutersBlockEntities.ROUTER_BLOCK_ENTITY.get(),
                (world, blockPos, thisBlockState, blockEntity) -> blockEntity.tick());
    }
}
