package com.benbenlaw.routers.item;

import net.minecraft.world.item.Item;

public class UpgradeItem extends Item {

    private final int extractValuePerOperation;

    public UpgradeItem(Properties properties, int extractValuePerOperation) {
        super(properties);
        this.extractValuePerOperation = extractValuePerOperation;
    }

    public int getExtractAmount() {
        return extractValuePerOperation;
    }
/*

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> components, TooltipFlag flag) {

        if (Screen.hasShiftDown()) {

            if (stack.is(RoutersTags.Items.RF_UPGRADES)) {
                components.add(Component.translatable("tooltip.routers.rf_upgrade", extractValuePerOperation).withStyle(ChatFormatting.YELLOW));
            }
            if (stack.is(RoutersTags.Items.ITEM_UPGRADES)) {
                components.add(Component.translatable("tooltip.routers.item_upgrade", extractValuePerOperation).withStyle(ChatFormatting.YELLOW));
            }
            if (stack.is(RoutersTags.Items.FLUID_UPGRADES)) {
                components.add(Component.translatable("tooltip.routers.fluid_upgrade", extractValuePerOperation).withStyle(ChatFormatting.YELLOW));
            }
            if (stack.is(RoutersTags.Items.CHEMICAL_UPGRADES)) {
                components.add(Component.translatable("tooltip.routers.chemical_upgrade", extractValuePerOperation).withStyle(ChatFormatting.YELLOW));
            }
            if (stack.is(RoutersTags.Items.SPEED_UPGRADES)) {
                components.add(Component.translatable("tooltip.routers.speed_upgrade", extractValuePerOperation).withStyle(ChatFormatting.YELLOW));
            }
            if (stack.is(RoutersTags.Items.SOURCE_UPGRADES)) {
                components.add(Component.translatable("tooltip.routers.source_upgrade", extractValuePerOperation).withStyle(ChatFormatting.YELLOW));
            }
            if (stack.is(RoutersItems.STACK_ROUND_ROBIN_UPGRADE)) {
                components.add(Component.translatable("tooltip.routers.stack_round_robin_upgrade").withStyle(ChatFormatting.YELLOW));
            } else if (stack.is(RoutersTags.Items.ROUND_ROBIN_UPGRADES)) {
                components.add(Component.translatable("tooltip.routers.round_robin_upgrade", extractValuePerOperation).withStyle(ChatFormatting.YELLOW));
            }
            if (stack.is(RoutersTags.Items.SOUL_UPGRADES)) {
                components.add(Component.translatable("tooltip.routers.soul_upgrade", extractValuePerOperation).withStyle(ChatFormatting.YELLOW));
            }
            if (stack.is(RoutersTags.Items.PRESSURE_UPGRADES)) {
                components.add(Component.translatable("tooltip.routers.pressure_upgrade", extractValuePerOperation).withStyle(ChatFormatting.YELLOW));
            }
            if (stack.is(RoutersTags.Items.HEAT_UPGRADES_PC)) {
                components.add(Component.translatable("tooltip.routers.heat_upgrade_pc", extractValuePerOperation).withStyle(ChatFormatting.YELLOW));
            }
            if (stack.is(RoutersTags.Items.DIMENSIONAL_UPGRADES)) {
                components.add(Component.translatable("tooltip.routers.dimensional_upgrade").withStyle(ChatFormatting.YELLOW));
            }
        } else {
            components.add(Component.translatable("tooltip.routers.hold_shift").withStyle(ChatFormatting.YELLOW));
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (stack.is(RoutersTags.Items.FILTERS)) {

            BlockPos pos = player.blockPosition();

            ContainerData data = new SimpleContainerData(2);

            player.openMenu(new SimpleMenuProvider(
                    (windowId, playerInventory, playerEntity) -> new ConfigMenu(windowId, playerInventory, pos, data),
                    Component.translatable("screen.routers.config_screen")), (buf -> buf.writeBlockPos(pos)));
        }
        return InteractionResultHolder.success(player.getItemInHand(hand));
    }

 */
}
