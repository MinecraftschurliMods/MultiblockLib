package com.github.minecraftschurlimods.multiblocklib.impl;

import com.github.minecraftschurlimods.multiblocklib.api.MBAPI;
import com.github.minecraftschurlimods.multiblocklib.api.Multiblock;
import com.github.minecraftschurlimods.multiblocklib.api.StateMatcher;
import com.github.minecraftschurlimods.multiblocklib.xplat.XplatAbstractions;
import com.mojang.serialization.Codec;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class MBAPIImpl implements MBAPI {

    @Nullable
    @Override
    public Multiblock getMultiblock(ResourceLocation id) {
        return RegistryAccess.BUILTIN.get().registryOrThrow(MBAPI.MULTIBLOCK_REGISTRY).get(id);
    }

    @Override
    public ResourceLocation getMultiblockId(Multiblock multiblock) {
        return RegistryAccess.BUILTIN.get().registryOrThrow(MBAPI.MULTIBLOCK_REGISTRY).getKey(multiblock);
    }

    @Override
    public Codec<Codec<? extends Multiblock>> getMultiblockTypeRegistryCodec() {
        return XplatAbstractions.INSTANCE.getMultiblockTypeRegistryCodec();
    }

    @Override
    public Codec<Codec<? extends StateMatcher>> getStateMatcherTypeRegistryCodec() {
        return XplatAbstractions.INSTANCE.getStateMatcherTypeRegistryCodec();
    }
}
