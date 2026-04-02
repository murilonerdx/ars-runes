package br.com.murilo.ruinaarcana.magic;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.lang.reflect.Method;

public final class ArsSourceJarCompatHelper {

    private static final ResourceLocation SOURCE_JAR_ID = new ResourceLocation("ars_nouveau", "source_jar");
    private static final ResourceLocation SOURCE_JAR_TILE_ID = new ResourceLocation("ars_nouveau", "source_jar_tile");

    private ArsSourceJarCompatHelper() {
    }

    public static int pushToNearbySourceJars(ServerLevel level, BlockPos center, int radius, int maxAmount) {
        if (maxAmount <= 0) {
            return 0;
        }

        int remaining = maxAmount;
        int moved = 0;

        for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(-radius, -radius, -radius),
                center.offset(radius, radius, radius))) {

            if (remaining <= 0) {
                break;
            }

            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity == null || !isArsSourceJar(blockEntity)) {
                continue;
            }

            int added = addSourceByReflection(blockEntity, remaining);
            if (added <= 0) {
                continue;
            }

            moved += added;
            remaining -= added;
        }

        return moved;
    }

    private static boolean isArsSourceJar(BlockEntity blockEntity) {
        ResourceLocation key = BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(blockEntity.getType());
        return SOURCE_JAR_ID.equals(key) || SOURCE_JAR_TILE_ID.equals(key);
    }

    private static int addSourceByReflection(BlockEntity blockEntity, int maxAdd) {
        try {
            Integer current = invokeIntNoArgs(blockEntity, "getSource", "getCurrentSource", "getSourceAmount");
            Integer max = invokeIntNoArgs(blockEntity, "getMaxSource", "getSourceCapacity", "getMaxCapacity");

            if (current == null || max == null || max <= current) {
                return 0;
            }

            int room = Math.max(0, max - current);
            int accepted = Math.min(room, maxAdd);
            if (accepted <= 0) {
                return 0;
            }

            if (invokeBooleanIntArg(blockEntity, accepted, "addSource", "addSourceAmount")) {
                blockEntity.setChanged();
                return accepted;
            }

            if (invokeBooleanIntArg(blockEntity, current + accepted, "setSource", "setSourceAmount")) {
                blockEntity.setChanged();
                return accepted;
            }
        } catch (Throwable ignored) {
            return 0;
        }

        return 0;
    }

    private static Integer invokeIntNoArgs(Object target, String... names) {
        for (String name : names) {
            try {
                Method method = target.getClass().getMethod(name);
                method.setAccessible(true);
                Object result = method.invoke(target);
                if (result instanceof Number number) {
                    return number.intValue();
                }
            } catch (ReflectiveOperationException ignored) {
            }
        }

        return null;
    }

    private static boolean invokeBooleanIntArg(Object target, int value, String... names) {
        for (String name : names) {
            try {
                Method method = target.getClass().getMethod(name, int.class);
                method.setAccessible(true);
                Object result = method.invoke(target, value);
                if (result == null) {
                    return true;
                }
                if (result instanceof Boolean bool) {
                    return bool;
                }
                return true;
            } catch (ReflectiveOperationException ignored) {
            }
        }

        return false;
    }
}
