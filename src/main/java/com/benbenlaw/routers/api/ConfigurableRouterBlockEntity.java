package com.benbenlaw.routers.api;

import com.benbenlaw.core.block.entity.handler.fluid.FilterFluidHandler;
import com.benbenlaw.core.block.entity.handler.item.FilterItemHandler;
import com.benbenlaw.routers.screen.util.button.ButtonType;

public interface ConfigurableRouterBlockEntity {
    FilterItemHandler getFilterItemHandler();
    FilterFluidHandler getFilterFluidHandler();
    boolean hasUpgrade(ButtonType type);
}
