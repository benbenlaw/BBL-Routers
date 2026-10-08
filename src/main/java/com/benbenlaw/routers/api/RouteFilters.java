package com.benbenlaw.routers.api;

import com.benbenlaw.core.block.entity.handler.item.FilterItemHandler;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

public record RouteFilters(@Nullable ConfigurableRouterBlockEntity exporter, boolean exporterBlacklist,
                           @Nullable ConfigurableRouterBlockEntity importer, boolean importerBlacklist) {

    public static final RouteFilters NONE = new RouteFilters(null, false, null, false);

    public <K> boolean passes(Identifier resource, Function<ItemStack, K> keyOf, K wanted) {
        return passes(exporter, exporterBlacklist, resource, keyOf, wanted)
                && passes(importer, importerBlacklist, resource, keyOf, wanted);
    }

    private static <K> boolean passes(@Nullable ConfigurableRouterBlockEntity router, boolean blacklist, Identifier resource, Function<ItemStack, K> keyOf, K wanted) {
        if (router == null) return true;

        FilterItemHandler handler = router.getResourceFilter(resource);
        Set<K> keys = new HashSet<>();
        for (int i = 0; i < handler.size(); i++) {
            ItemStack stack = handler.getResource(i).toStack();
            if (stack.isEmpty()) continue;

            K key = keyOf.apply(stack);
            if (key != null) keys.add(key);
        }

        if (keys.isEmpty()) return true;
        return blacklist != keys.contains(wanted);
    }
}
