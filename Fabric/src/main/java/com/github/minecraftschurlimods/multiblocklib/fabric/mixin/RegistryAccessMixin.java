package com.github.minecraftschurlimods.multiblocklib.fabric.mixin;

import com.github.minecraftschurlimods.multiblocklib.api.MBAPI;
import com.github.minecraftschurlimods.multiblocklib.api.Multiblock;
import com.google.common.collect.ImmutableMap;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(RegistryAccess.class)
public interface RegistryAccessMixin {
    @ModifyVariable(method = "method_30531", name = "builder", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableMap$Builder;build()Lcom/google/common/collect/ImmutableMap;", shift = At.Shift.BEFORE))
    private static ImmutableMap.Builder<ResourceKey<? extends Registry<?>>, RegistryAccess.RegistryData<?>> fillRegistryDataMap(ImmutableMap.Builder<ResourceKey<? extends Registry<?>>, RegistryAccess.RegistryData<?>> value) {
        value.put(MBAPI.MULTIBLOCK_REGISTRY, new RegistryAccess.RegistryData<>(MBAPI.MULTIBLOCK_REGISTRY, Multiblock.DIRECT_CODEC, null));
        return value;
    }
}
