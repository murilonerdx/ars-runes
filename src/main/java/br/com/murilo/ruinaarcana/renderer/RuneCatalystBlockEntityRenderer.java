package br.com.murilo.ruinaarcana.renderer;

import br.com.murilo.ruinaarcana.block.entity.RuneCatalystBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class RuneCatalystBlockEntityRenderer implements BlockEntityRenderer<RuneCatalystBlockEntity> {

    public RuneCatalystBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(RuneCatalystBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {

        ItemStack stack = blockEntity.getStoredRune();
        if (stack.isEmpty()) {
            return;
        }

        poseStack.pushPose();

        poseStack.translate(0.5D, 1.05D, 0.5D);

        double time = blockEntity.getLevel() != null ? blockEntity.getLevel().getGameTime() + partialTick : 0;
        poseStack.translate(0.0D, Math.sin(time * 0.08D) * 0.03D, 0.0D);
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees((float) ((time * 2.0D) % 360D)));

        poseStack.scale(0.6F, 0.6F, 0.6F);

        Minecraft.getInstance().getItemRenderer().renderStatic(
                stack,
                ItemDisplayContext.GROUND,
                packedLight,
                packedOverlay,
                poseStack,
                buffer,
                blockEntity.getLevel(),
                0
        );

        poseStack.popPose();
    }
}