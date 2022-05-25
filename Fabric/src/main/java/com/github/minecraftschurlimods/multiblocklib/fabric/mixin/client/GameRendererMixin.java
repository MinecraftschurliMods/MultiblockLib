package com.github.minecraftschurlimods.multiblocklib.fabric.mixin.client;

import com.github.minecraftschurlimods.multiblocklib.api.client.MultiblockVisualisationRenderer;
import com.github.minecraftschurlimods.multiblocklib.client.MultiblockVisualisationRendererImpl;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
abstract class GameRendererMixin {
    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V", ordinal = 1))
    private void onRenderLevelLast(final float f, final long l, final PoseStack poseStack, final CallbackInfo ci) {
        ((MultiblockVisualisationRendererImpl) MultiblockVisualisationRenderer.INSTANCE).renderMultiblock(poseStack);
    }
}
