package com.benbenlaw.routers.item;

import com.benbenlaw.routers.networking.packets.FilterItemUpdate;
import com.benbenlaw.routers.screen.ConfigMenu;
import com.benbenlaw.routers.util.RoutersTags;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

public class FilterItem extends Item {


    public FilterItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {

        if (!level.isClientSide) {
            BlockPos pos = player.blockPosition();

            ContainerData data = new SimpleContainerData(2);

            player.openMenu(new SimpleMenuProvider(
                    (windowId, playerInventory, playerEntity) -> new ConfigMenu(windowId, playerInventory, pos, data),
                    Component.translatable("screen.routers.config_screen")), (buf -> buf.writeBlockPos(pos)));

        }
        return InteractionResultHolder.success(player.getItemInHand(hand));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> components, TooltipFlag flag) {

        if (Screen.hasShiftDown()) {

            if (stack.is(RoutersItems.TAG_FILTER)) {
                if (stack.has(RoutersDataComponents.TAG_FILTER.get())) {
                    ResourceLocation tag = stack.get(RoutersDataComponents.TAG_FILTER.get());
                    assert tag != null;
                    components.add(Component.translatable("tooltip.routers.tag_filter", tag.toString()).withStyle(ChatFormatting.YELLOW));
                } else {
                    components.add(Component.translatable("tooltip.routers.tag_filter_empty").withStyle(ChatFormatting.YELLOW));
                }
            }
            if (stack.is(RoutersItems.MOD_FILTER)) {
                if (stack.has(RoutersDataComponents.MOD_FILTER.get())) {
                    String mod = stack.get(RoutersDataComponents.MOD_FILTER.get());
                    components.add(Component.translatable("tooltip.routers.mod_filter", mod).withStyle(ChatFormatting.YELLOW));
                } else {
                    components.add(Component.translatable("tooltip.routers.mod_filter_empty").withStyle(ChatFormatting.YELLOW));
                }
            }

        } else {
            components.add(Component.translatable("tooltip.routers.hold_shift").withStyle(ChatFormatting.YELLOW));
        }
    }

    public void setTag(ItemStack stack, ResourceLocation tag) {
        stack.set(RoutersDataComponents.TAG_FILTER.get(), tag);
        PacketDistributor.sendToServer(new FilterItemUpdate(tag == null ? "" : tag.toString()));
    }

    public void setMod(ItemStack stack, String mod) {
        stack.set(RoutersDataComponents.MOD_FILTER.get(), mod);
        PacketDistributor.sendToServer(new FilterItemUpdate(mod == null ? "" : mod));
    }

    public TagKey<Item> getTag(ItemStack stack) {
        if (stack.is(RoutersItems.TAG_FILTER) && stack.has(RoutersDataComponents.TAG_FILTER.get())) {
            ResourceLocation tagLocation = stack.get(RoutersDataComponents.TAG_FILTER.get());
            if (tagLocation != null) {
                return TagKey.create(net.minecraft.core.registries.Registries.ITEM, tagLocation);
            }
        }
        return null;
    }


}
