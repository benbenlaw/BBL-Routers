package com.benbenlaw.routers.item;

import com.benbenlaw.routers.block.ExporterBlock;
import com.benbenlaw.routers.block.ImporterBlock;
import com.benbenlaw.routers.block.RouterBlock;
import com.benbenlaw.routers.block.entity.ImporterBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class ConnectorItem extends Item {
    public ConnectorItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        // Sneaking skips the block's own handling, so Sneak + Right Click on a Router always
        // saves it as the importer position, even when the connector already holds one
        Level level = context.getLevel();
        Player player = context.getPlayer();
        BlockPos pos = context.getClickedPos();
        if (player == null || !player.isSecondaryUseActive() || !(level.getBlockState(pos).getBlock() instanceof RouterBlock)) {
            return super.useOn(context);
        }

        if (!level.isClientSide()) {
            context.getItemInHand().set(RoutersDataComponents.IMPORTER_POSITION, pos);
            player.displayClientMessage(Component.translatable("message.routers.importer.added_pos"), true);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }



    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> components, TooltipFlag flag) {

        if (Screen.hasShiftDown()) {
            components.add(Component.translatable("tooltip.routers.router_connector").withStyle(ChatFormatting.YELLOW));
            BlockPos importerPos = stack.get(RoutersDataComponents.IMPORTER_POSITION.get());
            if (importerPos != null) {
                components.add(Component.literal("Importer Pos: " + importerPos.getX() + ", " + importerPos.getY() + ", " + importerPos.getZ()));
            } else {
                components.add(Component.literal("No Importer Pos Set").withStyle(ChatFormatting.RED));
            }
        } else {
            components.add(Component.translatable("tooltip.routers.hold_shift").withStyle(ChatFormatting.YELLOW));
        }


    }
}
