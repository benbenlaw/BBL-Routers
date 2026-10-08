package com.benbenlaw.routers.integration.thaumaturge;

import com.benbenlaw.routers.networking.packets.SetFilterSlotById;
import com.benbenlaw.routers.screen.upgrade.FilterScreen;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.ingredients.ITypedIngredient;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.inventory.Slot;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.ArrayList;
import java.util.List;

public class ThaumaturgeJei {

    public static <I> List<IGhostIngredientHandler.Target<I>> targets(FilterScreen gui, ITypedIngredient<I> ingredient) {
        List<IGhostIngredientHandler.Target<I>> targets = new ArrayList<>();
        if (!(ingredient.getIngredient() instanceof AspectInstance instance)) return targets;

        var key = instance.aspect().unwrapKey().orElse(null);
        if (key == null) return targets;

        for (Slot slot : gui.getMenu().slots) {
            if (!(slot instanceof AspectFilterSlot)) continue;

            Rect2i bounds = new Rect2i(gui.getLeftPos() + slot.x, gui.getTopPos() + slot.y, 16, 16);
            targets.add(new IGhostIngredientHandler.Target<I>() {
                @Override
                public Rect2i getArea() {
                    return bounds;
                }

                @Override
                public void accept(I ignored) {
                    Level level = Minecraft.getInstance().level;
                    ((AspectFilterSlot) slot).setById(key.identifier(), level);
                    ClientPacketDistributor.sendToServer(new SetFilterSlotById(slot.index, key.identifier()));
                }
            });
        }
        return targets;
    }
}
