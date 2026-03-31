package br.com.murilo.ruinaarcana.magic;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

public final class ArcaneChargeHelper {

    private static final String CHARGE_KEY = "ArcaneCharge";

    private ArcaneChargeHelper() {
    }

    public static boolean isChargeable(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof ArcaneChargeable;
    }

    public static int getMaxCharge(ItemStack stack) {
        if (!isChargeable(stack)) {
            return 0;
        }
        return ((ArcaneChargeable) stack.getItem()).getMaxCharge(stack);
    }

    public static int getCharge(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(CHARGE_KEY)) {
            return 0;
        }
        return Math.max(0, tag.getInt(CHARGE_KEY));
    }

    public static int setCharge(ItemStack stack, int value) {
        if (!isChargeable(stack)) {
            return 0;
        }
        int clamped = Math.max(0, Math.min(value, getMaxCharge(stack)));
        stack.getOrCreateTag().putInt(CHARGE_KEY, clamped);
        return clamped;
    }

    public static int addCharge(ItemStack stack, int amount) {
        if (amount <= 0 || !isChargeable(stack)) {
            return 0;
        }

        int current = getCharge(stack);
        int max = getMaxCharge(stack);
        int accepted = Math.min(amount, Math.max(0, max - current));

        if (accepted > 0) {
            setCharge(stack, current + accepted);
        }

        return accepted;
    }

    public static int removeCharge(ItemStack stack, int amount) {
        if (amount <= 0 || !isChargeable(stack)) {
            return 0;
        }

        int current = getCharge(stack);
        int removed = Math.min(amount, current);

        if (removed > 0) {
            setCharge(stack, current - removed);
        }

        return removed;
    }

    public static int transfer(ItemStack source, ItemStack target, int maxAmount) {
        if (maxAmount <= 0 || source == target || !isChargeable(source) || !isChargeable(target)) {
            return 0;
        }

        int sourceCharge = getCharge(source);
        int targetRoom = getMaxCharge(target) - getCharge(target);
        int moved = Math.min(maxAmount, Math.min(sourceCharge, Math.max(0, targetRoom)));

        if (moved <= 0) {
            return 0;
        }

        removeCharge(source, moved);
        addCharge(target, moved);
        return moved;
    }

    public static int chargeInventoryFromStack(ItemStack source, Player player, int maxAmount) {
        if (maxAmount <= 0 || player == null) {
            return 0;
        }

        int moved = 0;

        for (ItemStack stack : player.getInventory().items) {
            if (stack == source) {
                continue;
            }
            moved += transfer(source, stack, maxAmount - moved);
            if (moved >= maxAmount) {
                return moved;
            }
        }

        for (ItemStack stack : player.getInventory().offhand) {
            if (stack == source) {
                continue;
            }
            moved += transfer(source, stack, maxAmount - moved);
            if (moved >= maxAmount) {
                return moved;
            }
        }

        return moved;
    }

    public static int chargeNearbyItemEntitiesFromStack(ServerLevel level, ItemStack source, double x, double y, double z, double radius, int maxAmount) {
        if (maxAmount <= 0) {
            return 0;
        }

        int moved = 0;
        AABB area = new AABB(x, y, z, x, y, z).inflate(radius);

        for (ItemEntity itemEntity : level.getEntitiesOfClass(ItemEntity.class, area, entity -> entity.isAlive() && !entity.getItem().isEmpty())) {
            ItemStack target = itemEntity.getItem();
            moved += transfer(source, target, maxAmount - moved);
            if (moved >= maxAmount) {
                break;
            }
        }

        return moved;
    }

    public static int chargeInventoryFromReservoir(Player player, int available, int maxAmount) {
        int remaining = Math.min(available, maxAmount);
        int moved = 0;

        for (ItemStack stack : player.getInventory().items) {
            if (remaining <= 0) {
                break;
            }
            int accepted = addCharge(stack, remaining);
            moved += accepted;
            remaining -= accepted;
        }

        for (ItemStack stack : player.getInventory().offhand) {
            if (remaining <= 0) {
                break;
            }
            int accepted = addCharge(stack, remaining);
            moved += accepted;
            remaining -= accepted;
        }

        return moved;
    }

    public static int chargeNearbyItemEntitiesFromReservoir(ServerLevel level, double x, double y, double z, double radius, int available, int maxAmount) {
        int remaining = Math.min(available, maxAmount);
        int moved = 0;
        AABB area = new AABB(x, y, z, x, y, z).inflate(radius);

        for (ItemEntity itemEntity : level.getEntitiesOfClass(ItemEntity.class, area, entity -> entity.isAlive() && !entity.getItem().isEmpty())) {
            if (remaining <= 0) {
                break;
            }
            int accepted = addCharge(itemEntity.getItem(), remaining);
            moved += accepted;
            remaining -= accepted;
        }

        return moved;
    }
}
