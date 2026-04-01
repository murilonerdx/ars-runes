package br.com.murilo.ruinaarcana.renderer;

import br.com.murilo.ruinaarcana.RuinaArcanaMod;
import br.com.murilo.ruinaarcana.renderer.state.ArcaneHarvestBenchRangeState;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = RuinaArcanaMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class ArcaneHarvestBenchRangeRenderer {

    private ArcaneHarvestBenchRangeRenderer() {
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }

        if (!ArcaneHarvestBenchRangeState.isEnabledForCurrentLevel(minecraft.level)) {
            return;
        }

        BlockPos center = ArcaneHarvestBenchRangeState.getBenchPos();
        if (center == null) {
            return;
        }

        int range = ArcaneHarvestBenchRangeState.getRange();
        if (range <= 0) {
            return;
        }

        Vec3 camera = event.getCamera().getPosition();
        PoseStack poseStack = event.getPoseStack();

        double minX = center.getX() - range;
        double minY = center.getY() - 1;
        double minZ = center.getZ() - range;
        double maxX = center.getX() + range + 1;
        double maxY = center.getY() + 3;
        double maxZ = center.getZ() + range + 1;

        AABB fullBox = new AABB(minX, minY, minZ, maxX, maxY, maxZ)
                .move(-camera.x, -camera.y, -camera.z);

        AABB floorBox = new AABB(minX, center.getY(), minZ, maxX, center.getY() + 1.0D, maxZ)
                .move(-camera.x, -camera.y, -camera.z);

        MultiBufferSource.BufferSource bufferSource = minecraft.renderBuffers().bufferSource();
        VertexConsumer lineConsumer = bufferSource.getBuffer(RenderType.lines());

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();

        poseStack.pushPose();
        LevelRenderer.renderLineBox(poseStack, lineConsumer, fullBox, 0.20F, 0.85F, 1.00F, 1.00F);
        LevelRenderer.renderLineBox(poseStack, lineConsumer, floorBox, 0.75F, 0.35F, 1.00F, 1.00F);
        poseStack.popPose();

        bufferSource.endBatch(RenderType.lines());

        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }
}