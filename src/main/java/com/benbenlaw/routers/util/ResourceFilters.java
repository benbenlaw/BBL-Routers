package com.benbenlaw.routers.util;

import com.benbenlaw.core.block.entity.SyncableBlockEntity;
import com.benbenlaw.core.block.entity.handler.item.FilterItemHandler;
import com.benbenlaw.routers.api.RouterButtonTypes;
import com.benbenlaw.routers.screen.util.button.ButtonType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;

import java.util.LinkedHashMap;
import java.util.Map;

public class ResourceFilters {

    private final SyncableBlockEntity host;
    private final Map<Identifier, FilterItemHandler> handlers = new LinkedHashMap<>();

    public ResourceFilters(SyncableBlockEntity host) {
        this.host = host;
    }

    public FilterItemHandler get(Identifier resource) {
        return handlers.computeIfAbsent(resource, id -> new FilterItemHandler(host, 18));
    }

    public boolean any() {
        for (FilterItemHandler handler : handlers.values()) {
            if (!ResourceHandlerUtil.isEmpty(handler)) return true;
        }
        return false;
    }

    public void save(ValueOutput output) {
        ValueOutput root = null;

        for (Map.Entry<Identifier, FilterItemHandler> entry : handlers.entrySet()) {
            if (ResourceHandlerUtil.isEmpty(entry.getValue())) continue;

            if (root == null) root = output.child("resourceFilters");
            entry.getValue().serialize(root.child(entry.getKey().toString()));
        }
    }

    public void load(ValueInput input) {
        ValueInput root = input.childOrEmpty("resourceFilters");

        for (ButtonType type : RouterButtonTypes.all()) {
            root.child(type.getId().toString()).ifPresent(saved -> get(type.getId()).deserialize(saved));
        }
    }
}
