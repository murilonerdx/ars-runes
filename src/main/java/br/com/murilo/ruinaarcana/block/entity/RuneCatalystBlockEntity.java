package br.com.murilo.ruinaarcana.block.entity;

import br.com.murilo.ruinaarcana.config.RuinaArcanaConfig;
import br.com.murilo.ruinaarcana.magic.ArcaneChargeHelper;
import br.com.murilo.ruinaarcana.magic.ArcaneEnergyNetworkHelper;
import br.com.murilo.ruinaarcana.registry.ModBlockEntities;
import br.com.murilo.ruinaarcana.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class RuneCatalystBlockEntity extends BlockEntity {

    private static final String STORED_RUNE_KEY = "StoredRune";

    private ItemStack storedRune = ItemStack.EMPTY;

    public RuneCatalystBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CATALISADOR_DE_RUNAS.get(), pos, state);
    }

    public boolean insertRune(ItemStack stack) {
        if (!storedRune.isEmpty() || stack.isEmpty() || !stack.is(ModItems.RUNA_DA_RUINA.get())) {
            return false;
        }

        ItemStack copy = stack.copy();
        copy.setCount(1);
        storedRune = copy;
        setChanged();
        return true;
    }

    public ItemStack removeRune() {
        if (storedRune.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack removed = storedRune.copy();
        storedRune = ItemStack.EMPTY;
        setChanged();
        return removed;
    }

    public ItemStack getStoredRune() {
        return storedRune;
    }

    public int getStoredRuneCharge() {
        return storedRune.isEmpty() ? 0 : ArcaneChargeHelper.getCharge(storedRune);
    }

    public int getStoredRuneMaxCharge() {
        return storedRune.isEmpty() ? 0 : ArcaneChargeHelper.getMaxCharge(storedRune);
    }

    public void serverTick() {
        if (level == null || level.isClientSide || storedRune.isEmpty()) {
            return;
        }

        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        int interval = Math.max(1, RuinaArcanaConfig.VALUES.runeCatalystPulseIntervalTicks.get());
        if (level.getGameTime() % interval != 0L) {
            return;
        }

        int current = ArcaneChargeHelper.getCharge(storedRune);
        int max = ArcaneChargeHelper.getMaxCharge(storedRune);
        if (current >= max) {
            return;
        }

        int needed = max - current;
        int maxPull = Math.min(needed, RuinaArcanaConfig.VALUES.runeCatalystChargePerPulse.get());

        int pulled = ArcaneEnergyNetworkHelper.pullFromNearbyBatteries(
                serverLevel,
                worldPosition,
                RuinaArcanaConfig.VALUES.runeCatalystBatteryRadius.get(),
                maxPull
        );

        if (pulled > 0) {
            ArcaneChargeHelper.addCharge(storedRune, pulled);
            setChanged();
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);

        if (!storedRune.isEmpty()) {
            tag.put(STORED_RUNE_KEY, storedRune.save(new CompoundTag()));
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        if (tag.contains(STORED_RUNE_KEY)) {
            storedRune = ItemStack.of(tag.getCompound(STORED_RUNE_KEY));
        } else {
            storedRune = ItemStack.EMPTY;
        }
    }
}