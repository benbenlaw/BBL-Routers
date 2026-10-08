package com.benbenlaw.routers.integration.thaumaturge;

import com.benbenlaw.routers.api.RouteFilters;
import com.benbenlaw.routers.api.transfers.DistributorHandlers;
import com.benbenlaw.routers.block.entity.DistributorBlockEntity;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaCapabilities;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaTransport;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class EssentiaDistributor implements IEssentiaTransport {

    private record Machine(IEssentiaTransport transport, Direction side) {}

    private final DistributorBlockEntity distributor;
    private final DistributorHandlers.Spreader spreader = new DistributorHandlers.Spreader();

    public EssentiaDistributor(DistributorBlockEntity distributor) {
        this.distributor = distributor;
    }

    private List<Machine> machines(Holder<IAspect> aspect) {
        List<Machine> machines = new ArrayList<>();
        if (!(distributor.getLevel() instanceof ServerLevel level)) return machines;

        for (DistributorBlockEntity.Target target : distributor.getTargets(level)) {
            IEssentiaTransport transport = target.get(EssentiaCapabilities.TRANSPORT, level);
            Direction side = target.side(EssentiaCapabilities.TRANSPORT);
            if (transport != null && transport.canInputFrom(side)) machines.add(new Machine(transport, side));
        }
        return machines;
    }

    private int distribute(Holder<IAspect> aspect, int amount, boolean simulate) {
        if (amount <= 0) return 0;

        ResourceKey<IAspect> key = aspect.unwrapKey().orElse(null);
        if (key == null || !new RouteFilters(distributor, false, null, false).passes(ThaumaturgeIntegration.ESSENTIA, EssentiaRouterTransfer::aspectOf, key)) return 0;

        List<Machine> machines = machines(aspect);
        return spreader.spread(amount, machines.size(), false, !simulate,
                (index, share) -> machines.get(index).transport().addEssentia(aspect, share, machines.get(index).side(), simulate));
    }

    @Override
    public int addEssentia(Holder<IAspect> aspect, int amount, Direction face) {
        return distribute(aspect, amount, false);
    }

    @Override
    public int addEssentia(Holder<IAspect> aspect, int amount, Direction face, boolean simulate) {
        return distribute(aspect, amount, simulate);
    }

    @Override
    public int spaceFor(Holder<IAspect> aspect, Direction face) {
        long space = 0;
        for (Machine machine : machines(aspect)) space += machine.transport().spaceFor(aspect, machine.side());
        return (int) Math.min(space, Integer.MAX_VALUE);
    }

    @Override
    public boolean isConnectable(Direction face) {
        return true;
    }

    @Override
    public boolean canInputFrom(Direction face) {
        return true;
    }

    @Override
    public boolean canOutputTo(Direction face) {
        return false;
    }

    @Override
    public void setSuction(Holder<IAspect> aspect, int amount) {
    }

    @Nullable
    @Override
    public Holder<IAspect> getSuctionType(Direction face) {
        return null;
    }

    @Override
    public int getSuctionAmount(Direction face) {
        return 0;
    }

    @Override
    public int takeEssentia(Holder<IAspect> aspect, int amount, Direction face) {
        return 0;
    }

    @Nullable
    @Override
    public Holder<IAspect> getEssentiaType(Direction face) {
        return null;
    }

    @Override
    public int getEssentiaAmount(Direction face) {
        return 0;
    }

    @Override
    public int getMinimumSuction() {
        return 0;
    }
}
