package br.com.murilo.ruinaarcana.block.entity;

import br.com.murilo.ruinaarcana.block.ArcaneBatteryBlock;
import br.com.murilo.ruinaarcana.config.RuinaArcanaConfig;
import br.com.murilo.ruinaarcana.magic.ArcaneChargeHelper;
import br.com.murilo.ruinaarcana.magic.ArsSourceJarCompatHelper;
import br.com.murilo.ruinaarcana.menu.ArcaneBatteryMenu;
import br.com.murilo.ruinaarcana.registry.ModBlockEntities;
import br.com.murilo.ruinaarcana.registry.ModBlocks;
import br.com.murilo.ruinaarcana.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.ModList;

public class ArcaneBatteryBlockEntity extends BlockEntity implements MenuProvider {

    private static final String CHARGE_KEY = "StoredCharge";
    private static final String CATALYST_LINKED_KEY = "CatalystLinked";

    private final ContainerData menuData = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> charge;
                case 1 -> getMaxCharge();
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            if (index == 0) {
                setCharge(value);
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    };

    private int charge;
    private boolean catalystLinked;

    public ArcaneBatteryBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.BATERIA_ARCANA.get(), pos, blockState);
    }

    public static ArcaneBatteryBlockEntity createClientDummy(BlockPos pos) {
        return new ArcaneBatteryBlockEntity(
                pos,
                ModBlocks.BATERIA_ARCANA.get().defaultBlockState()
        );
    }

    public boolean installCatalystLink(ItemStack catalystStack) {
        if (catalystLinked || catalystStack.isEmpty() || !catalystStack.is(ModItems.CATALISADOR_MAGICO.get())) {
            return false;
        }

        catalystLinked = true;

        int initialCharge = ArcaneChargeHelper.getCharge(catalystStack);
        if (initialCharge > 0) {
            addCharge(initialCharge);
        } else {
            syncChargeStage();
        }

        setChanged();
        return true;
    }

    public boolean hasCatalystLink() {
        return catalystLinked;
    }

    public void serverTick() {
        if (level == null || level.isClientSide) {
            return;
        }

        int interval = Math.max(1, RuinaArcanaConfig.VALUES.batteryPulseIntervalTicks.get());
        if (level.getGameTime() % interval != 0L) {
            return;
        }

        int max = getMaxCharge();
        if (max <= 0) {
            return;
        }

        // Sem catalisador vinculado, a bateria só mantém o estado visual.
        if (!catalystLinked) {
            syncChargeStage();
            return;
        }

        if (charge < max) {
            // Recarrega infinitamente após o vínculo do catalisador.
            int linkedGain = Math.max(1, RuinaArcanaConfig.VALUES.catalystSkyChargePerPulse.get());
            addCharge(linkedGain);
        }

        int sourceJarPull = Math.max(0, RuinaArcanaConfig.VALUES.batterySourceJarTransferPerPulse.get());
        if (ModList.get().isLoaded("ars_nouveau") && sourceJarPull > 0 && charge > 0) {
            int moved = ArsSourceJarCompatHelper.pushToNearbySourceJars(
                    (ServerLevel) level,
                    worldPosition,
                    RuinaArcanaConfig.VALUES.batterySourceJarRadius.get(),
                    Math.min(sourceJarPull, charge)
            );
            if (moved > 0) {
                removeCharge(moved);
            }
        }

        syncChargeStage();
    }

    public int extractCharge(int requested) {
        int removed = Math.min(Math.max(0, requested), charge);
        if (removed > 0) {
            removeCharge(removed);
        }
        return removed;
    }

    public int getCharge() {
        return charge;
    }

    public int getMaxCharge() {
        return RuinaArcanaConfig.VALUES.batteryMaxCharge.get();
    }

    public int getComparatorLevel() {
        if (getMaxCharge() <= 0) {
            return 0;
        }

        return Math.max(
                0,
                Math.min(15, (int) Math.round((charge / (double) getMaxCharge()) * 15.0D))
        );
    }

    public void addCharge(int amount) {
        if (amount <= 0) {
            return;
        }

        setCharge(this.charge + amount);
    }

    public void removeCharge(int amount) {
        if (amount <= 0) {
            return;
        }

        setCharge(this.charge - amount);
    }

    private void setCharge(int newCharge) {
        int clamped = Math.max(0, Math.min(getMaxCharge(), newCharge));
        if (this.charge == clamped) {
            syncChargeStage();
            return;
        }

        this.charge = clamped;
        setChanged();
        syncChargeStage();
    }

    private void syncChargeStage() {
        if (level == null) {
            return;
        }

        BlockState currentState = level.getBlockState(worldPosition);
        if (!(currentState.getBlock() instanceof ArcaneBatteryBlock)) {
            return;
        }

        int max = Math.max(1, getMaxCharge());
        int stage = Math.min(4, (int) Math.floor((charge / (double) max) * 5.0D));
        if (stage >= 5) {
            stage = 4;
        }

        if (currentState.getValue(ArcaneBatteryBlock.CHARGE_STAGE) != stage) {
            level.setBlock(worldPosition, currentState.setValue(ArcaneBatteryBlock.CHARGE_STAGE, stage), 3);
        } else {
            level.sendBlockUpdated(worldPosition, currentState, currentState, 3);
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide) {
            syncChargeStage();
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.ruinaarcana.bateria_arcana");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new ArcaneBatteryMenu(containerId, playerInventory, this, menuData);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt(CHARGE_KEY, charge);
        tag.putBoolean(CATALYST_LINKED_KEY, catalystLinked);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.charge = Math.max(0, tag.getInt(CHARGE_KEY));
        this.catalystLinked = tag.getBoolean(CATALYST_LINKED_KEY);
    }
}
