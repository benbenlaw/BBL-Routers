package com.benbenlaw.routers.api.transfers;

import com.benbenlaw.core.block.entity.handler.item.FilterItemHandler;
import com.benbenlaw.routers.block.entity.DistributorBlockEntity;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntBinaryOperator;

public class DistributorHandlers {

    public static class Items implements ResourceHandler<ItemResource> {

        private final DistributorBlockEntity distributor;
        private final Spreader spreader = new Spreader();

        public Items(DistributorBlockEntity distributor) {
            this.distributor = distributor;
        }

        @Override
        public int insert(ItemResource resource, int amount, TransactionContext transaction) {
            if (resource.isEmpty() || amount <= 0) return 0;
            if (!(distributor.getLevel() instanceof ServerLevel level)) return 0;

            FilterItemHandler filter = distributor.getFilterItemHandler();
            if (!ResourceHandlerUtil.isEmpty(filter)
                    && ItemTransfer.checkFilter(filter, resource, true, false) <= 0) {
                return 0;
            }

            List<ResourceHandler<ItemResource>> handlers = new ArrayList<>();
            for (DistributorBlockEntity.Target target : distributor.getTargets(level)) {
                ResourceHandler<ItemResource> handler = target.get(Capabilities.Item.BLOCK, level);
                if (handler != null) handlers.add(handler);
            }

            return spreader.spread(amount, handlers.size(), false,
                    (index, share) -> handlers.get(index).insert(resource, share, transaction));
        }

        @Override public int size() { return 0; }
        @Override public ItemResource getResource(int index) { return ItemResource.EMPTY; }
        @Override public long getAmountAsLong(int index) { return 0; }
        @Override public long getCapacityAsLong(int index, ItemResource resource) { return 0; }
        @Override public boolean isValid(int index, ItemResource resource) { return false; }
        @Override public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) { return 0; }
        @Override public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) { return 0; }
    }

    public static class Fluids implements ResourceHandler<FluidResource> {

        private final DistributorBlockEntity distributor;
        private final Spreader spreader = new Spreader();

        public Fluids(DistributorBlockEntity distributor) {
            this.distributor = distributor;
        }

        @Override
        public int insert(FluidResource resource, int amount, TransactionContext transaction) {
            if (resource.isEmpty() || amount <= 0) return 0;
            if (!(distributor.getLevel() instanceof ServerLevel level)) return 0;

            if (!ResourceHandlerUtil.isEmpty(distributor.getFilterFluidHandler())
                    && !distributor.getFilterFluidHandler().matchesFluid(resource, true, false)) {
                return 0;
            }

            List<ResourceHandler<FluidResource>> handlers = new ArrayList<>();
            for (DistributorBlockEntity.Target target : distributor.getTargets(level)) {
                ResourceHandler<FluidResource> handler = target.get(Capabilities.Fluid.BLOCK, level);
                if (handler != null) handlers.add(handler);
            }

            return spreader.spread(amount, handlers.size(), false,
                    (index, share) -> handlers.get(index).insert(resource, share, transaction));
        }

        @Override public int size() { return 0; }
        @Override public FluidResource getResource(int index) { return FluidResource.EMPTY; }
        @Override public long getAmountAsLong(int index) { return 0; }
        @Override public long getCapacityAsLong(int index, FluidResource resource) { return 0; }
        @Override public boolean isValid(int index, FluidResource resource) { return false; }
        @Override public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) { return 0; }
        @Override public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) { return 0; }
    }

    public static class Energy implements EnergyHandler {

        private final DistributorBlockEntity distributor;
        private final Spreader spreader = new Spreader();

        public Energy(DistributorBlockEntity distributor) {
            this.distributor = distributor;
        }

        @Override
        public int insert(int amount, TransactionContext transaction) {
            if (amount <= 0) return 0;
            if (!(distributor.getLevel() instanceof ServerLevel level)) return 0;

            List<EnergyHandler> handlers = new ArrayList<>();
            for (DistributorBlockEntity.Target target : distributor.getTargets(level)) {
                EnergyHandler handler = target.get(Capabilities.Energy.BLOCK, level);
                if (handler != null) handlers.add(handler);
            }

            return spreader.spread(amount, handlers.size(), false,
                    (index, share) -> handlers.get(index).insert(share, transaction));
        }

        @Override public long getAmountAsLong() { return 0; }
        @Override public long getCapacityAsLong() { return 0; }
        @Override public int extract(int amount, TransactionContext transaction) { return 0; }
    }

    public static final class Spreader {

        private int next;

        public int spread(int total, int count, boolean single, IntBinaryOperator insertInto) {
            return spread(total, count, single, true, insertInto);
        }

        // rotate = false looks without moving the starting machine on, so a trial run and the real thing share out the same way
        public int spread(int total, int count, boolean single, boolean rotate, IntBinaryOperator insertInto) {
            if (count <= 0 || total <= 0) return 0;

            int start = next % count;

            if (single) {
                for (int i = 0; i < count; i++) {
                    int index = (start + i) % count;
                    int moved = insertInto.applyAsInt(index, total);
                    if (moved > 0) {
                        if (rotate) next = index + 1;
                        return moved;
                    }
                }
                return 0;
            }

            int accepted = 0;
            int remaining = total;

            for (int i = 0; i < count && remaining > 0; i++) {
                int share = (remaining + (count - i) - 1) / (count - i);
                int moved = insertInto.applyAsInt((start + i) % count, Math.min(share, remaining));
                accepted += moved;
                remaining -= moved;
            }

            for (int i = 0; i < count && remaining > 0; i++) {
                int moved = insertInto.applyAsInt((start + i) % count, remaining);
                accepted += moved;
                remaining -= moved;
            }

            if (rotate) next = start + 1;
            return accepted;
        }
    }
}
