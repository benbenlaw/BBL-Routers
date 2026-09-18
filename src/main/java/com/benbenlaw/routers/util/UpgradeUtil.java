package com.benbenlaw.routers.util;

import com.benbenlaw.core.block.entity.handler.item.SyncableItemHandler;
import com.benbenlaw.routers.screen.util.button.ButtonType;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemResource;

import java.util.List;

public class UpgradeUtil {

    public static List<TagKey<Item>> getUpgradeTypeTags() {
        return List.of(
                RoutersTags.Items.SPEED_UPGRADES,
                RoutersTags.Items.ITEM_UPGRADES,
                RoutersTags.Items.FLUID_UPGRADES,
                RoutersTags.Items.RF_UPGRADES
        );
    }

    public static boolean hasUpgradeTypeAlready(SyncableItemHandler upgradeItemHandler, ItemStack stack) {
        for (int i = 0; i < upgradeItemHandler.size(); i++) {
            ItemStack existing = upgradeItemHandler.getResource(i).toStack();
            if (existing.isEmpty()) continue;

            for (TagKey<Item> tag : getUpgradeTypeTags()) {
                if (stack.is(tag) && existing.is(tag)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean hasUpgrade(SyncableItemHandler upgradeItemHandler, ButtonType type) {
        for (int i = 0; i < upgradeItemHandler.size(); i++) {
            ItemResource resource = upgradeItemHandler.getResource(i);
            if (resource.toStack().is(type.getUnlockedBy())) {
                return true;
            }
        }
        return false;
    }
}
