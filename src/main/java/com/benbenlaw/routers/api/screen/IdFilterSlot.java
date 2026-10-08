package com.benbenlaw.routers.api.screen;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public interface IdFilterSlot {

    void clickWith(ItemStack carried, Level level);

    void setById(Identifier id, Level level);
}
