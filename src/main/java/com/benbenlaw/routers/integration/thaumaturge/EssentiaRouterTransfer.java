package com.benbenlaw.routers.integration.thaumaturge;

import com.benbenlaw.routers.api.RouteFilters;
import com.benbenlaw.routers.api.transfers.SimpleCapabilityTransfer;
import com.benbenlaw.routers.block.entity.DistributorBlockEntity;
import com.benbenlaw.routers.util.RoutersTags;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaAccess;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaCapabilities;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaItemStorage;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaTransport;
import com.leclowndu93150.thaumaturge.api.essentia.IItemEssentia;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.capabilities.BlockCapability;

public class EssentiaRouterTransfer extends SimpleCapabilityTransfer<IEssentiaTransport> {

    @Override
    public TagKey<Item> upgradeTag() {
        return RoutersTags.Items.ESSENTIA_UPGRADES;
    }

    @Override
    public BlockCapability<IEssentiaTransport, Direction> capability() {
        return EssentiaCapabilities.TRANSPORT;
    }

    @Override
    protected boolean isEmpty(IEssentiaTransport source, Direction side) {
        return !source.canOutputTo(side) || source.getEssentiaType(side) == null || source.getEssentiaAmount(side) <= 0;
    }

    @Override
    protected boolean move(IEssentiaTransport source, Direction sourceSide, IEssentiaTransport target, Direction targetSide, int amount, RouteFilters filters) {
        if (!source.canOutputTo(sourceSide) || !target.canInputFrom(targetSide)) return false;

        Holder<IAspect> aspect = source.getEssentiaType(sourceSide);
        if (aspect == null) return false;

        ResourceKey<IAspect> key = aspect.unwrapKey().orElse(null);
        if (key == null || !filters.passes(ThaumaturgeIntegration.ESSENTIA, EssentiaRouterTransfer::aspectOf, key)) return false;

        int offered = source.takeEssentia(aspect, amount, sourceSide, true);
        if (offered <= 0) return false;

        int fits = target.addEssentia(aspect, offered, targetSide, true);
        if (fits <= 0) return false;

        int taken = source.takeEssentia(aspect, fits, sourceSide, false);
        if (taken <= 0) return false;

        int added = target.addEssentia(aspect, taken, targetSide, false);
        if (added < taken) source.addEssentia(aspect, taken - added, sourceSide, false);

        return added > 0;
    }

    // The aspect a filter slot stands for: a label set to an aspect, or a phial with essentia in it. Anything else is ignored.
    @Nullable
    public static ResourceKey<IAspect> aspectOf(ItemStack stack) {
        ResourceKey<IAspect> label = EssentiaAccess.aspectFilter(stack);
        if (label != null) return label;

        IEssentiaItemStorage storage = stack.getCapability(EssentiaCapabilities.ITEM_STORAGE, null);
        if (storage != null) {
            for (AspectInstance entry : storage.contents().entries()) {
                if (entry.amount() > 0) return entry.aspect().unwrapKey().orElse(null);
            }
        }

        IItemEssentia container = stack.getCapability(EssentiaCapabilities.CONTAINER, null);
        if (container != null) {
            for (AspectInstance entry : container.getAspects().entries()) {
                if (entry.amount() > 0) return entry.aspect().unwrapKey().orElse(null);
            }
        }

        return null;
    }

    @Override
    public boolean acceptsInput(IEssentiaTransport handler, Direction face) {
        return handler.isConnectable(face) && handler.canInputFrom(face);
    }

    @Override
    public IEssentiaTransport createDistributor(DistributorBlockEntity distributor) {
        return new EssentiaDistributor(distributor);
    }
}
