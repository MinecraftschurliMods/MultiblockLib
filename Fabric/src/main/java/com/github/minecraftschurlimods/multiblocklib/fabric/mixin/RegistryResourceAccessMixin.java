package com.github.minecraftschurlimods.multiblocklib.fabric.mixin;

import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net/minecraft/resources/RegistryResourceAccess$1")
abstract class RegistryResourceAccessMixin {
    @Inject(method = "registryDirPath", at = @At("HEAD"), cancellable = true)
    private static void inject(ResourceLocation $$0, CallbackInfoReturnable<String> cir) {
        if (!$$0.getNamespace().equals("minecraft")) {
            cir.setReturnValue($$0.getNamespace() + "/" + $$0.getPath());
        }
    }
}
