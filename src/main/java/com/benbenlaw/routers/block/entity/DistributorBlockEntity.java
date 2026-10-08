package com.benbenlaw.routers.block.entity;

import com.benbenlaw.core.block.entity.SyncableBlockEntity;
import com.benbenlaw.core.block.entity.handler.fluid.FilterFluidHandler;
import com.benbenlaw.core.block.entity.handler.item.FilterItemHandler;
import com.benbenlaw.core.block.entity.handler.item.SyncableItemHandler;
import com.benbenlaw.routers.api.ConfigurableRouterBlockEntity;
import com.benbenlaw.routers.api.NamedRouter;
import com.benbenlaw.routers.api.TransferModule;
import com.benbenlaw.routers.block.RoutersBlockEntities;
import com.benbenlaw.routers.block.custom.RouterBlock;
import com.benbenlaw.routers.config.StartupConfig;
import com.benbenlaw.routers.screen.DistributorMenu;
import com.benbenlaw.routers.screen.util.button.ButtonType;
import com.benbenlaw.routers.transfers.RoutersTransfers;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Never ticks and has no storage or upgrades of its own. Exporters link to it like an importer, and whatever they push
// into it is passed straight on to the machines around it. Like an Importer, its filter buttons come from what the
// linked exporters carry, and its filters decide what gets through.
public class DistributorBlockEntity extends SyncableBlockEntity implements MenuProvider, ConfigurableRouterBlockEntity, ImporterHost, NamedRouter {

    private final ImporterCore importerCore = new ImporterCore(this);

    // Upgrades a Distributor saved before it stopped taking any. They aren't usable, just handed back when it's broken.
    private final SyncableItemHandler legacyUpgrades = new SyncableItemHandler(this, 9, (i, stack) -> false, i -> false);

    private String routerName = "";

    private final Map<TransferModule<?>, Object> distributors = new HashMap<>();

    private List<Target> targets = List.of();
    private long nextRefresh;

    public DistributorBlockEntity(BlockPos pos, BlockState state) {
        super(RoutersBlockEntities.DISTRIBUTOR_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    public ImporterCore getImporterCore() {
        return importerCore;
    }

    @Override
    public FilterItemHandler getFilterItemHandler() {
        return importerCore.getFilterItemHandler();
    }

    @Override
    public FilterItemHandler getResourceFilter(Identifier resource) {
        return importerCore.getResourceFilter(resource);
    }

    @Override
    public boolean hasResourceFilter() {
        return importerCore.hasResourceFilter();
    }

    @Override
    public FilterFluidHandler getFilterFluidHandler() {
        return importerCore.getFilterFluidHandler();
    }

    // Which filter buttons are available: whatever the linked exporters carry.
    @Override
    public boolean hasUpgrade(ButtonType type) {
        return importerCore.hasUpgrade(type);
    }

    @Override
    public String getRouterName() {
        return routerName;
    }

    @Override
    public void setRouterName(String name) {
        this.routerName = NamedRouter.clean(name);
        setChanged();
        sync();
    }

    // What an exporter pushes into for this resource. The exporter's own upgrade decides whether it sends the resource at
    // all, so there's nothing to check here.
    @Nullable
    @SuppressWarnings("unchecked")
    public <H> H getDistributor(TransferModule<H> module) {
        return (H) distributors.computeIfAbsent(module, key -> module.createDistributor(this));
    }

    public List<Target> getTargets(ServerLevel serverLevel) {
        long now = serverLevel.getGameTime();
        if (now >= nextRefresh) {
            nextRefresh = now + StartupConfig.distributorRefreshTicks.get();
            refreshTargets(serverLevel);
        }
        return targets;
    }

    private record Candidate(BlockPos pos, Map<BlockCapability<?, Direction>, Direction> faces) {}

    private void refreshTargets(ServerLevel serverLevel) {
        int range = StartupConfig.distributorRange.get();
        BlockPos origin = worldPosition;

        List<Candidate> found = new ArrayList<>();

        for (int chunkX = (origin.getX() - range) >> 4; chunkX <= (origin.getX() + range) >> 4; chunkX++) {
            for (int chunkZ = (origin.getZ() - range) >> 4; chunkZ <= (origin.getZ() + range) >> 4; chunkZ++) {
                LevelChunk chunk = serverLevel.getChunkSource().getChunkNow(chunkX, chunkZ);
                if (chunk == null) continue;

                for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
                    BlockPos pos = blockEntity.getBlockPos();
                    if (pos.equals(origin)) continue;
                    if (Math.abs(pos.getX() - origin.getX()) > range
                            || Math.abs(pos.getY() - origin.getY()) > range
                            || Math.abs(pos.getZ() - origin.getZ()) > range) continue;
                    if (blockEntity.getBlockState().getBlock() instanceof RouterBlock) continue;

                    Map<BlockCapability<?, Direction>, Direction> faces = probe(serverLevel, pos, sideToward(pos, origin));
                    if (!faces.isEmpty()) found.add(new Candidate(pos.immutable(), faces));
                }
            }
        }

        found.sort(Comparator.<Candidate>comparingDouble(candidate -> candidate.pos().distSqr(origin)).thenComparingLong(candidate -> candidate.pos().asLong()));
        int max = StartupConfig.distributorMaxTargets.get();
        if (found.size() > max) found = new ArrayList<>(found.subList(0, max));

        Map<BlockPos, Target> previous = new HashMap<>();
        for (Target target : targets) previous.put(target.pos, target);

        List<Target> refreshed = new ArrayList<>(found.size());
        for (Candidate candidate : found) {
            Target existing = previous.get(candidate.pos());
            boolean unchanged = existing != null && existing.faces.equals(candidate.faces());
            refreshed.add(unchanged ? existing : new Target(candidate.pos(), sideToward(candidate.pos(), origin), candidate.faces()));
        }
        targets = refreshed;
    }

    // For every resource, the face to reach this machine on: the one pointing at the distributor if it works, then the
    // unsided one, then whichever other face does. Some machines only expose a resource on one face (a jar's top, say).
    private static Map<BlockCapability<?, Direction>, Direction> probe(ServerLevel level, BlockPos pos, Direction toward) {
        Map<BlockCapability<?, Direction>, Direction> faces = new HashMap<>();
        for (TransferModule<?> module : RoutersTransfers.TRANSFER_MODULES_REGISTRY) {
            probe(level, module, pos, toward, faces);
        }
        return faces;
    }

    private static <H> void probe(ServerLevel level, TransferModule<H> module, BlockPos pos, Direction toward, Map<BlockCapability<?, Direction>, Direction> faces) {
        BlockCapability<H, Direction> capability = module.capability();

        if (usable(level, module, pos, toward, toward)) {
            faces.put(capability, toward);
        } else if (usable(level, module, pos, null, toward)) {
            faces.put(capability, null);
        } else {
            for (Direction face : Direction.values()) {
                if (face != toward && usable(level, module, pos, face, face)) {
                    faces.put(capability, face);
                    return;
                }
            }
        }
    }

    private static <H> boolean usable(ServerLevel level, TransferModule<H> module, BlockPos pos, @Nullable Direction context, Direction face) {
        H handler = level.getCapability(module.capability(), pos, context);
        return handler != null && module.acceptsInput(handler, face);
    }

    // the face of the machine that points at the distributor
    private static Direction sideToward(BlockPos machine, BlockPos distributor) {
        int dx = distributor.getX() - machine.getX();
        int dy = distributor.getY() - machine.getY();
        int dz = distributor.getZ() - machine.getZ();

        int ax = Math.abs(dx);
        int ay = Math.abs(dy);
        int az = Math.abs(dz);

        if (ax >= ay && ax >= az) return dx > 0 ? Direction.EAST : Direction.WEST;
        if (ay >= az) return dy > 0 ? Direction.UP : Direction.DOWN;
        return dz > 0 ? Direction.SOUTH : Direction.NORTH;
    }

    // One machine in range, with its capabilities looked up once and then kept up to date by the cache.
    public static final class Target {
        private final BlockPos pos;
        private final Direction side;
        // a null face means the unsided capability
        private final Map<BlockCapability<?, Direction>, Direction> faces;
        private final Map<BlockCapability<?, Direction>, BlockCapabilityCache<?, Direction>> caches = new HashMap<>();

        private Target(BlockPos pos, Direction side, Map<BlockCapability<?, Direction>, Direction> faces) {
            this.pos = pos;
            this.side = side;
            this.faces = faces;
        }

        // The face of the machine this resource is reached on, for capabilities whose calls take one.
        public Direction side(BlockCapability<?, Direction> capability) {
            Direction face = faces.get(capability);
            return face != null ? face : side;
        }

        @Nullable
        @SuppressWarnings("unchecked")
        public <H> H get(BlockCapability<H, Direction> capability, ServerLevel level) {
            if (!faces.containsKey(capability)) return null;

            BlockCapabilityCache<H, Direction> cache = (BlockCapabilityCache<H, Direction>) caches.computeIfAbsent(
                    capability, key -> BlockCapabilityCache.create(capability, level, pos, faces.get(capability)));
            return cache.getCapability();
        }
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.routers.distributor");
    }

    @Override
    public AbstractContainerMenu createMenu(int container, @NotNull Inventory inventory, @NotNull Player player) {
        return new DistributorMenu(container, inventory, getBlockPos());
    }

    @Override
    protected void saveAdditional(@NotNull ValueOutput output) {
        importerCore.save(output.child("linkedExporters"));
        if (!routerName.isEmpty()) output.putString("routerName", routerName);

        for (int i = 0; i < legacyUpgrades.size(); i++) {
            if (!legacyUpgrades.getResource(i).isEmpty()) {
                legacyUpgrades.serialize(output.child("upgradeItems"));
                break;
            }
        }

        super.saveAdditional(output);
    }

    @Override
    protected void loadAdditional(@NotNull ValueInput input) {
        importerCore.load(input.childOrEmpty("linkedExporters"));
        routerName = NamedRouter.clean(input.getStringOr("routerName", ""));

        legacyUpgrades.deserialize(input.childOrEmpty("upgradeItems"));

        // filters a Distributor saved when it was built like an Exporter
        input.child("itemFilter").ifPresent(filter -> importerCore.getFilterItemHandler().deserialize(filter));
        input.child("fluidFilter").ifPresent(filter -> importerCore.getFilterFluidHandler().deserialize(filter));

        super.loadAdditional(input);
    }

    public void preRemoveSideEffects(@NonNull BlockPos pos, @NonNull BlockState state) {
        dropInventoryContents(legacyUpgrades);
        importerCore.unlinkFromExporters(pos);
    }
}
