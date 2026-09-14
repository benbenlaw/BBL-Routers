package com.benbenlaw.routers.item;

import com.benbenlaw.routers.networking.packets.SyncFilterValue;
import com.benbenlaw.routers.networking.packets.SyncStockFilter;
import com.benbenlaw.routers.screen.ClientScreens;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public class FilterItem extends Item {

    public FilterType filterType;

    public FilterItem(Properties properties, FilterType filterType) {
        super(properties);
        this.filterType = filterType;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide()) {
            return InteractionResult.PASS;
        }

        if (!player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        ClientScreens.openConfigScreen(player.getMainHandItem());
        return InteractionResult.SUCCESS;
    }

    public void setTag(ItemStack stack, Identifier tag) {
        stack.set(RoutersDataComponents.TAG_FILTER.get(), tag);
        ClientPacketDistributor.sendToServer(new SyncFilterValue(SyncFilterValue.FilterField.TAG, tag == null ? "" : tag.toString()));
    }

    public void setMod(ItemStack stack, String mod) {
        stack.set(RoutersDataComponents.MOD_FILTER.get(), mod);
        ClientPacketDistributor.sendToServer(new SyncFilterValue(SyncFilterValue.FilterField.MOD, mod == null ? "" : mod));
    }

    public void setStock(ItemStack stack, ItemStack stockStack, int amount) {
        stack.set(RoutersDataComponents.STOCK_FILTER.get(), new StockFilter(stockStack, amount));
        ClientPacketDistributor.sendToServer(new SyncStockFilter(stockStack.copyWithCount(1), amount));
    }

    public TagKey<Item> getTag(ItemStack stack) {
        if (stack.is(RoutersItems.TAG_FILTER) && stack.has(RoutersDataComponents.TAG_FILTER.get())) {
            Identifier tagLocation = stack.get(RoutersDataComponents.TAG_FILTER.get());
            if (tagLocation != null) {
                return TagKey.create(net.minecraft.core.registries.Registries.ITEM, tagLocation);
            }
        }
        return null;
    }

    public boolean matches(ItemStack filterStack, net.minecraft.world.item.ItemStack incoming) {

        if (filterStack.isEmpty()) return true;

        if (!(filterStack.getItem() instanceof FilterItem filter)) return true;

        return switch (filter.filterType) {

            case MOD -> {
                String mod = filterStack.get(RoutersDataComponents.MOD_FILTER.get());
                if (mod == null) yield false;

                yield incoming.getItem().builtInRegistryHolder()
                        .unwrapKey()
                        .map(key -> key.identifier().getNamespace().equals(mod))
                        .orElse(false);
            }

            case TAG -> {
                Identifier id = filterStack.get(RoutersDataComponents.TAG_FILTER.get());
                if (id == null) yield false;

                var tag = TagKey.create(net.minecraft.core.registries.Registries.ITEM, id);

                yield incoming.is(tag);
            }

            case STOCK -> {
                var stock = filterStack.get(RoutersDataComponents.STOCK_FILTER.get());
                if (stock == null) yield false;

                yield ItemStack.isSameItemSameComponents(incoming, stock.stack());
            }
        };
    }


}
