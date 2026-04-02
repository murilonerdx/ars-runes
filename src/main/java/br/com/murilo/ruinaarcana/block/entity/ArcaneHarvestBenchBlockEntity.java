package br.com.murilo.ruinaarcana.block.entity;

import br.com.murilo.ruinaarcana.config.RuinaArcanaConfig;
import br.com.murilo.ruinaarcana.magic.ArcaneChargeHelper;
import br.com.murilo.ruinaarcana.magic.ArcaneEnergyNetworkHelper;
import br.com.murilo.ruinaarcana.magic.TemporalFieldLogic;
import br.com.murilo.ruinaarcana.menu.ArcaneHarvestBenchMenu;
import br.com.murilo.ruinaarcana.registry.ModBlockEntities;
import br.com.murilo.ruinaarcana.registry.ModBlocks;
import br.com.murilo.ruinaarcana.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CactusBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.SugarCaneBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArcaneHarvestBenchBlockEntity extends BlockEntity implements MenuProvider {

    private static final String CHARGE_KEY = "StoredCharge";
    private static final String ITEMS_KEY = "Inventory";
    private static final String RUNE_INVENTORY_KEY = "RuneInventory";
    private static final String LEGACY_RUNE_KEY = "InstalledRune";
    private static final String LINKED_POS_KEY = "LinkedInventoryPos";
    private static final String CATALYST_LINKED_KEY = "CatalystLinked";
    private static final String WORK_RANGE_KEY = "WorkRange";

    private static final int MIN_WORK_RANGE = 1;
    private static final int MAX_WORK_RANGE = 8;

    private final ItemStackHandler inventory = new ItemStackHandler(18) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    private final ItemStackHandler runeInventory = new ItemStackHandler(1) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return ModItems.isFarmRune(stack);
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> inventory);
    private final LazyOptional<IItemHandler> runeHandler = LazyOptional.of(() -> runeInventory);

    private final ContainerData menuData = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> charge;
                case 1 -> getMaxCharge();
                case 2 -> hasLinkedInventory() ? 1 : 0;
                case 3 -> getStoredStacks();
                case 4 -> workRange;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> charge = Math.max(0, value);
                case 4 -> workRange = Mth.clamp(value, MIN_WORK_RANGE, MAX_WORK_RANGE);
            }
        }

        @Override
        public int getCount() {
            return 5;
        }
    };

    private int charge;
    private int workRange;
    private boolean catalystLinked;

    @Nullable
    private BlockPos linkedInventoryPos;

    public ArcaneHarvestBenchBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.BANCADA_COLHEITA_ARCANA.get(), pos, blockState);
        this.workRange = getDefaultWorkRange();
    }

    public static ArcaneHarvestBenchBlockEntity createClientDummy(BlockPos pos) {
        return new ArcaneHarvestBenchBlockEntity(pos, ModBlocks.BANCADA_COLHEITA_ARCANA.get().defaultBlockState());
    }

    private int getDefaultWorkRange() {
        return Mth.clamp(RuinaArcanaConfig.VALUES.harvestBenchWorkRadius.get(), MIN_WORK_RANGE, MAX_WORK_RANGE);
    }

    public int getWorkRange() {
        return workRange;
    }

    public boolean changeWorkRange(int delta) {
        int newValue = Mth.clamp(workRange + delta, MIN_WORK_RANGE, MAX_WORK_RANGE);
        if (newValue == workRange) {
            return false;
        }

        workRange = newValue;
        setChanged();
        return true;
    }

    public boolean installCatalystLink(ItemStack catalystStack) {
        if (catalystLinked || catalystStack.isEmpty() || !catalystStack.is(ModItems.CATALISADOR_MAGICO.get())) {
            return false;
        }

        catalystLinked = true;

        int initialCharge = ArcaneChargeHelper.getCharge(catalystStack);
        if (initialCharge > 0) {
            charge = Math.min(getMaxCharge(), charge + initialCharge);
        }

        setChanged();
        return true;
    }

    public boolean installRune(ItemStack stack) {
        if (hasInstalledRune() || stack.isEmpty() || !ModItems.isFarmRune(stack)) {
            return false;
        }

        ItemStack copy = stack.copy();
        copy.setCount(1);
        runeInventory.setStackInSlot(0, copy);
        setChanged();
        return true;
    }

    public ItemStack removeInstalledRune() {
        ItemStack removed = runeInventory.getStackInSlot(0).copy();
        runeInventory.setStackInSlot(0, ItemStack.EMPTY);
        setChanged();
        return removed;
    }

    public boolean hasInstalledRune() {
        return !runeInventory.getStackInSlot(0).isEmpty();
    }

    private ItemStack getInstalledRune() {
        return runeInventory.getStackInSlot(0);
    }

    private boolean consumeInstalledRuneCharge(int amount) {
        ItemStack rune = getInstalledRune();
        if (rune.isEmpty() || !ModItems.isFarmRune(rune)) {
            return false;
        }

        if (ArcaneChargeHelper.getCharge(rune) < amount) {
            return false;
        }

        ArcaneChargeHelper.removeCharge(rune, amount);
        setChanged();
        return true;
    }

    private BenchRuneType getInstalledRuneType() {
        ItemStack rune = getInstalledRune();
        if (rune.is(ModItems.RUNA_DA_COLHEITA.get())) {
            return BenchRuneType.COLHEITA;
        }
        if (rune.is(ModItems.RUNA_DA_VITALIDADE.get())) {
            return BenchRuneType.VITALIDADE;
        }
        if (rune.is(ModItems.RUNA_DO_FLUXO.get())) {
            return BenchRuneType.FLUXO;
        }
        if (rune.is(ModItems.RUNA_DO_ARMAZENAMENTO.get())) {
            return BenchRuneType.ARMAZENAMENTO;
        }
        if (rune.is(ModItems.RUNA_DA_RUINA.get())) {
            return BenchRuneType.RUINA;
        }
        return BenchRuneType.NONE;
    }

    private void pullLinkedCatalystEnergy() {
        if (!catalystLinked) {
            return;
        }

        int room = Math.max(0, getMaxCharge() - charge);
        if (room <= 0) {
            return;
        }

        int gain = Math.max(1, RuinaArcanaConfig.VALUES.catalystSkyChargePerPulse.get());
        charge = Math.min(getMaxCharge(), charge + Math.min(room, gain));
        setChanged();
    }

    public void serverTick() {
        if (level == null || level.isClientSide) {
            return;
        }

        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        if (level.getGameTime() % 20L != 0L) {
            return;
        }

        pullLinkedCatalystEnergy();
        pullEnergy(serverLevel);

        if (charge <= 0) {
            return;
        }

        harvestCrops(serverLevel);
        harvestAnimals(serverLevel);
        collectLooseDrops(serverLevel);
        pushToLinkedInventory(serverLevel);
        triggerInstalledRunePulse(serverLevel);

        setChanged();
    }

    private void triggerInstalledRunePulse(ServerLevel level) {
        if (getInstalledRuneType() != BenchRuneType.RUINA) {
            return;
        }

        int machineCost = Math.max(1, RuinaArcanaConfig.VALUES.harvestBenchCropEnergyCost.get() / 2);
        int runePulseCost = Math.max(1, RuinaArcanaConfig.VALUES.runePulseEnergyCost.get());
        if (charge < machineCost) {
            return;
        }

        if (!consumeInstalledRuneCharge(runePulseCost)) {
            return;
        }

        consumeCharge(machineCost);
        TemporalFieldLogic.pulse(level, worldPosition);
    }

    private void pullEnergy(ServerLevel level) {
        int maxPull = RuinaArcanaConfig.VALUES.harvestBenchPullPerPulse.get();
        if (getInstalledRuneType() == BenchRuneType.FLUXO && consumeInstalledRuneCharge(4)) {
            maxPull += Math.max(1, maxPull / 2);
        }
        int room = Math.max(0, getMaxCharge() - charge);

        if (room <= 0) {
            return;
        }

        int pulled = ArcaneEnergyNetworkHelper.pullFromNearbyBatteries(
                level,
                worldPosition,
                RuinaArcanaConfig.VALUES.harvestBenchBatteryRadius.get(),
                Math.min(maxPull, room)
        );

        if (pulled > 0) {
            charge = Math.min(getMaxCharge(), charge + pulled);
            setChanged();
        }
    }

    private void accelerateGrowth(ServerLevel level) {
        int radius = getWorkRange();
        int machineCost = Math.max(1, RuinaArcanaConfig.VALUES.harvestBenchCropEnergyCost.get() / 2);
        int budget = 12;

        for (BlockPos pos : BlockPos.betweenClosed(
                worldPosition.offset(-radius, -1, -radius),
                worldPosition.offset(radius, 2, radius))) {

            if (budget <= 0 || charge < machineCost) {
                return;
            }

            BlockState state = level.getBlockState(pos);
            if (!state.isRandomlyTicking()) {
                continue;
            }

            state.randomTick(level, pos, level.random);

            if (charge >= machineCost * 2) {
                state.randomTick(level, pos, level.random);
                consumeCharge(machineCost);
            }

            consumeCharge(machineCost);
            budget--;
        }
    }

    private void harvestCrops(ServerLevel level) {
        int radius = getWorkRange();
        int baseCost = RuinaArcanaConfig.VALUES.harvestBenchCropEnergyCost.get();
        boolean harvestRuneBoost = getInstalledRuneType() == BenchRuneType.COLHEITA && consumeInstalledRuneCharge(1);
        int cost = harvestRuneBoost ? Math.max(1, (int) Math.floor(baseCost * 0.6D)) : baseCost;

        for (BlockPos pos : BlockPos.betweenClosed(
                worldPosition.offset(-radius, -1, -radius),
                worldPosition.offset(radius, 2, radius))) {

            if (charge < cost) {
                return;
            }

            BlockState state = level.getBlockState(pos);

            if (state.getBlock() instanceof CropBlock crop && crop.isMaxAge(state)) {
                List<ItemStack> drops = Block.getDrops(state, level, pos, null);
                for (ItemStack drop : drops) {
                    storeOrDrop(level, pos, drop);
                }
                level.setBlock(pos, crop.getStateForAge(0), 3);
                consumeCharge(cost);
                continue;
            }

            if (state.getBlock() instanceof NetherWartBlock
                    && state.hasProperty(NetherWartBlock.AGE)
                    && state.getValue(NetherWartBlock.AGE) >= 3) {
                storeOrDrop(level, pos, new ItemStack(state.getBlock().asItem(), 2 + level.random.nextInt(3)));
                level.setBlock(pos, state.setValue(NetherWartBlock.AGE, 0), 3);
                consumeCharge(cost);
                continue;
            }

            if (state.getBlock() instanceof SweetBerryBushBlock
                    && state.hasProperty(SweetBerryBushBlock.AGE)
                    && state.getValue(SweetBerryBushBlock.AGE) > 1) {
                storeOrDrop(level, pos, new ItemStack(state.getBlock().asItem(), 2 + level.random.nextInt(2)));
                level.setBlock(pos, state.setValue(SweetBerryBushBlock.AGE, 1), 3);
                consumeCharge(cost);
                continue;
            }

            if (state.getBlock() instanceof SugarCaneBlock || state.getBlock() instanceof CactusBlock) {
                BlockPos above = pos.above();
                BlockState aboveState = level.getBlockState(above);

                if (aboveState.getBlock() == state.getBlock()) {
                    storeOrDrop(level, above, new ItemStack(state.getBlock().asItem()));
                    level.destroyBlock(above, false);
                    consumeCharge(cost);
                }
            }
        }
    }

    private void harvestAnimals(ServerLevel level) {
        int baseCost = RuinaArcanaConfig.VALUES.harvestBenchAnimalEnergyCost.get();
        boolean vitalityRuneBoost = getInstalledRuneType() == BenchRuneType.VITALIDADE && consumeInstalledRuneCharge(2);
        int cost = vitalityRuneBoost ? Math.max(1, (int) Math.floor(baseCost * 0.65D)) : baseCost;
        if (charge < cost) {
            return;
        }

        int radius = getWorkRange();
        int keepCount = RuinaArcanaConfig.VALUES.harvestBenchAnimalKeepCount.get();
        AABB area = new AABB(worldPosition).inflate(radius + 0.5D);
        List<Animal> adults = level.getEntitiesOfClass(
                Animal.class,
                area,
                entity -> entity.isAlive() && !entity.isBaby()
        );

        if (adults.isEmpty()) {
            return;
        }

        Map<EntityType<?>, Integer> adultsByType = new HashMap<>();
        for (Animal adult : adults) {
            adultsByType.merge(adult.getType(), 1, Integer::sum);
        }

        for (Animal animal : adults) {
            if (charge < cost) {
                return;
            }

            if (animal instanceof Sheep sheep && sheep.isSheared()) {
                continue;
            }

            if (animal.getType() == EntityType.WOLF
                    || animal.getType() == EntityType.CAT
                    || animal.getType() == EntityType.HORSE) {
                continue;
            }

            int sameTypeAdults = adultsByType.getOrDefault(animal.getType(), 0);

            if (sameTypeAdults <= keepCount) {
                continue;
            }

            animal.hurt(level.damageSources().magic(), Float.MAX_VALUE);
            consumeCharge(cost);
            collectLooseDrops(level);
            return;
        }
    }

    private void collectLooseDrops(ServerLevel level) {
        int radius = getWorkRange();
        AABB area = new AABB(worldPosition).inflate(radius + 0.75D);

        for (ItemEntity itemEntity : level.getEntitiesOfClass(
                ItemEntity.class,
                area,
                entity -> entity.isAlive() && !entity.getItem().isEmpty())) {

            ItemStack remaining = ItemHandlerHelper.insertItemStacked(inventory, itemEntity.getItem().copy(), false);

            if (remaining.isEmpty()) {
                itemEntity.discard();
            } else if (remaining.getCount() != itemEntity.getItem().getCount()) {
                itemEntity.setItem(remaining);
            }
        }
    }

    private void pushToLinkedInventory(ServerLevel level) {
        if (linkedInventoryPos == null || charge <= 0) {
            return;
        }

        BlockEntity targetEntity = level.getBlockEntity(linkedInventoryPos);
        if (targetEntity == null) {
            return;
        }

        LazyOptional<IItemHandler> targetOptional = targetEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, null);
        if (!targetOptional.isPresent()) {
            return;
        }

        IItemHandler target = targetOptional.orElse(null);
        if (target == null) {
            return;
        }

        int remainingItemBudget = RuinaArcanaConfig.VALUES.harvestBenchTeleportItemsPerPulse.get();
        int energyPerItem = Math.max(1, RuinaArcanaConfig.VALUES.harvestBenchTeleportEnergyPerItem.get());
        if (getInstalledRuneType() == BenchRuneType.ARMAZENAMENTO && consumeInstalledRuneCharge(3)) {
            remainingItemBudget += 8;
            energyPerItem = Math.max(1, energyPerItem - 1);
        }

        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            if (remainingItemBudget <= 0 || charge < energyPerItem) {
                return;
            }

            ItemStack inSlot = inventory.getStackInSlot(slot);
            if (inSlot.isEmpty()) {
                continue;
            }

            ItemStack simulated = inventory.extractItem(slot, inSlot.getCount(), true);
            if (simulated.isEmpty()) {
                continue;
            }

            int transferableCount = Math.min(simulated.getCount(), Math.min(remainingItemBudget, charge / energyPerItem));
            if (transferableCount <= 0) {
                return;
            }

            ItemStack moving = simulated.copy();
            moving.setCount(transferableCount);

            ItemStack leftover = ItemHandlerHelper.insertItemStacked(target, moving, false);
            int moved = transferableCount - leftover.getCount();

            if (moved > 0) {
                inventory.extractItem(slot, moved, false);
                remainingItemBudget -= moved;
                consumeCharge(moved * energyPerItem);
            }
        }
    }

    private void storeOrDrop(ServerLevel level, BlockPos pos, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }

        ItemStack remaining = ItemHandlerHelper.insertItemStacked(inventory, stack.copy(), false);
        if (!remaining.isEmpty()) {
            Block.popResource(level, pos, remaining);
        }
    }

    private void consumeCharge(int amount) {
        if (amount <= 0) {
            return;
        }

        charge = Math.max(0, charge - amount);
        setChanged();
    }

    public void dropContents(ServerLevel level) {
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                Block.popResource(level, worldPosition, stack.copy());
                inventory.setStackInSlot(slot, ItemStack.EMPTY);
            }
        }

        ItemStack rune = runeInventory.getStackInSlot(0);
        if (!rune.isEmpty()) {
            Block.popResource(level, worldPosition.above(), rune.copy());
            runeInventory.setStackInSlot(0, ItemStack.EMPTY);
        }

        setChanged();
    }

    public ItemStackHandler getStorageHandler() {
        return inventory;
    }

    public ItemStackHandler getRuneHandler() {
        return runeInventory;
    }

    public void setLinkedInventoryPos(@Nullable BlockPos linkedInventoryPos) {
        this.linkedInventoryPos = linkedInventoryPos;
        setChanged();
    }

    @Nullable
    public BlockPos getLinkedInventoryPos() {
        return linkedInventoryPos;
    }

    public boolean hasLinkedInventory() {
        return linkedInventoryPos != null;
    }

    public int getStoredStacks() {
        int count = 0;
        for (int i = 0; i < inventory.getSlots(); i++) {
            if (!inventory.getStackInSlot(i).isEmpty()) {
                count++;
            }
        }
        return count;
    }

    public int getCharge() {
        return charge;
    }

    public int getMaxCharge() {
        return RuinaArcanaConfig.VALUES.harvestBenchMaxCharge.get();
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.ruinaarcana.bancada_colheita_arcana");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new ArcaneHarvestBenchMenu(containerId, playerInventory, this, menuData);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemHandler.invalidate();
        runeHandler.invalidate();
    }

    @Override
    public <T> LazyOptional<T> getCapability(net.minecraftforge.common.capabilities.Capability<T> cap,
                                             @Nullable net.minecraft.core.Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return itemHandler.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt(CHARGE_KEY, charge);
        tag.putInt(WORK_RANGE_KEY, workRange);
        tag.put(ITEMS_KEY, inventory.serializeNBT());
        tag.put(RUNE_INVENTORY_KEY, runeInventory.serializeNBT());
        tag.putBoolean(CATALYST_LINKED_KEY, catalystLinked);

        if (linkedInventoryPos != null) {
            tag.put(LINKED_POS_KEY, NbtUtils.writeBlockPos(linkedInventoryPos));
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        charge = Math.max(0, tag.getInt(CHARGE_KEY));
        workRange = tag.contains(WORK_RANGE_KEY)
                ? Mth.clamp(tag.getInt(WORK_RANGE_KEY), MIN_WORK_RANGE, MAX_WORK_RANGE)
                : getDefaultWorkRange();

        catalystLinked = tag.getBoolean(CATALYST_LINKED_KEY);
        inventory.deserializeNBT(tag.getCompound(ITEMS_KEY));

        if (tag.contains(RUNE_INVENTORY_KEY)) {
            runeInventory.deserializeNBT(tag.getCompound(RUNE_INVENTORY_KEY));
        } else {
            runeInventory.setStackInSlot(
                    0,
                    tag.contains(LEGACY_RUNE_KEY) ? ItemStack.of(tag.getCompound(LEGACY_RUNE_KEY)) : ItemStack.EMPTY
            );
        }

        linkedInventoryPos = tag.contains(LINKED_POS_KEY)
                ? NbtUtils.readBlockPos(tag.getCompound(LINKED_POS_KEY))
                : null;
    }

    private enum BenchRuneType {
        NONE,
        RUINA,
        COLHEITA,
        VITALIDADE,
        FLUXO,
        ARMAZENAMENTO
    }
}
