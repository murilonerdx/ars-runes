package br.com.murilo.ruinaarcana.menu;

import br.com.murilo.ruinaarcana.block.entity.ArcaneHarvestBenchBlockEntity;
import br.com.murilo.ruinaarcana.registry.ModBlocks;
import br.com.murilo.ruinaarcana.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.SlotItemHandler;

public class ArcaneHarvestBenchMenu extends AbstractContainerMenu {

    public static final int BUTTON_RANGE_DOWN = 0;
    public static final int BUTTON_RANGE_UP = 1;

    private static final int RUNE_SLOT = 0;
    private static final int STORAGE_START = 1;
    private static final int STORAGE_END = 19;
    private static final int MACHINE_SLOT_COUNT = 19;

    private final ArcaneHarvestBenchBlockEntity blockEntity;
    private final Level level;
    private final ContainerData data;

    public ArcaneHarvestBenchMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, extraData), new SimpleContainerData(5));
    }

    public ArcaneHarvestBenchMenu(int containerId, Inventory playerInventory, ArcaneHarvestBenchBlockEntity blockEntity, ContainerData data) {
        super(ModMenus.ARCANE_HARVEST_BENCH.get(), containerId);
        this.blockEntity = blockEntity;
        this.level = playerInventory.player.level();
        this.data = data;

        checkContainerDataCount(data, 5);
        addDataSlots(data);

        addMachineSlots();
        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);
    }

    private static ArcaneHarvestBenchBlockEntity getBlockEntity(Inventory playerInventory, FriendlyByteBuf extraData) {
        BlockPos pos = extraData.readBlockPos();
        BlockEntity blockEntity = playerInventory.player.level().getBlockEntity(pos);

        if (!(blockEntity instanceof ArcaneHarvestBenchBlockEntity bench)) {
            throw new IllegalStateException("Block entity inválida em " + pos);
        }

        return bench;
    }

    private void addMachineSlots() {
        this.addSlot(new SlotItemHandler(blockEntity.getRuneHandler(), 0, 18, 34));

        int storageStartX = 62;
        int storageStartY = 18;

        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 9; col++) {
                int slotIndex = col + (row * 9);
                int x = storageStartX + (col * 18);
                int y = storageStartY + (row * 18);
                this.addSlot(new SlotItemHandler(blockEntity.getStorageHandler(), slotIndex, x, y));
            }
        }
    }

    private void addPlayerInventory(Inventory inventory) {
        int startX = 36;
        int startY = 103;

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(
                        inventory,
                        col + row * 9 + 9,
                        startX + col * 18,
                        startY + row * 18
                ));
            }
        }
    }

    private void addPlayerHotbar(Inventory inventory) {
        int startX = 36;
        int y = 161;

        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(inventory, col, startX + col * 18, y));
        }
    }

    public BlockPos getBenchPos() {
        return blockEntity.getBlockPos();
    }

    public int getCharge() {
        return data.get(0);
    }

    public int getMaxCharge() {
        return data.get(1);
    }

    public boolean isLinked() {
        return data.get(2) == 1;
    }

    public int getStoredStacks() {
        return data.get(3);
    }

    public int getWorkRange() {
        return data.get(4);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (player.level().isClientSide) {
            return true;
        }

        return switch (id) {
            case BUTTON_RANGE_DOWN -> blockEntity.changeWorkRange(-1);
            case BUTTON_RANGE_UP -> blockEntity.changeWorkRange(1);
            default -> false;
        };
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack copied = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        copied = stack.copy();

        if (index < MACHINE_SLOT_COUNT) {
            if (!this.moveItemStackTo(stack, MACHINE_SLOT_COUNT, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else {
            if (blockEntity.getRuneHandler().isItemValid(0, stack)) {
                if (!this.moveItemStackTo(stack, RUNE_SLOT, RUNE_SLOT + 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(stack, STORAGE_START, STORAGE_END, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        if (stack.getCount() == copied.getCount()) {
            return ItemStack.EMPTY;
        }

        slot.onTake(player, stack);
        return copied;
    }

    @Override
    public boolean stillValid(Player player) {
        if (level.isClientSide) {
            return true;
        }

        return stillValid(
                ContainerLevelAccess.create(level, blockEntity.getBlockPos()),
                player,
                ModBlocks.BANCADA_COLHEITA_ARCANA.get()
        );
    }
}