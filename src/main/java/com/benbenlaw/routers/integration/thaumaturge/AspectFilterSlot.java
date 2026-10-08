package com.benbenlaw.routers.integration.thaumaturge;

import com.benbenlaw.routers.api.screen.IdFilterSlot;
import com.leclowndu93150.thaumaturge.api.aspect.Aspects;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.transfer.IndexModifier;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;
import org.jetbrains.annotations.Nullable;

public class AspectFilterSlot extends ResourceHandlerSlot implements IdFilterSlot {

    private static final Identifier LABEL = Identifier.fromNamespaceAndPath("thaumaturge", "label");

    public AspectFilterSlot(ResourceHandler<ItemResource> handler, IndexModifier<ItemResource> modifier, int index, int x, int y) {
        super(handler, modifier, index, x, y);
    }

    public static ItemStack labelFor(ResourceKey<IAspect> aspect) {
        return EssentiaAccess.withAspectFilter(new ItemStack(BuiltInRegistries.ITEM.getValue(LABEL)), aspect);
    }

    @Nullable
    public ResourceKey<IAspect> aspect() {
        return EssentiaAccess.aspectFilter(getItem());
    }

    @Override
    public void clickWith(ItemStack carried, Level level) {
        if (carried.isEmpty()) {
            set(ItemStack.EMPTY);
            return;
        }

        ResourceKey<IAspect> aspect = EssentiaRouterTransfer.aspectOf(carried);
        if (aspect != null) set(labelFor(aspect));
    }

    @Override
    public void setById(Identifier id, Level level) {
        ResourceKey<IAspect> key = ResourceKey.create(IAspect.REGISTRY_KEY, id);
        try {
            if (!Aspects.resolve(level, key).isBound()) return;
        } catch (RuntimeException e) {
            return;
        }
        set(labelFor(key));
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return false;
    }

    @Override
    public boolean mayPickup(Player player) {
        return false;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return 1;
    }

    @Override
    public boolean hasItem() {
        return false;
    }
}
