package com.github.minecraftschurlimods.multiblocklib.api;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.ServiceLoader;

public interface MBAPI {
    String MODID = "multiblocklib";
    MBAPI INSTANCE = ServiceLoader.load(MBAPI.class).findFirst().orElseThrow();
    ResourceKey<Registry<Codec<? extends Multiblock>>> MULTIBLOCK_TYPE_REGISTRY = ResourceKey.createRegistryKey(new ResourceLocation(MODID, "multiblock_type"));
    ResourceKey<Registry<Codec<? extends StateMatcher>>> STATE_MATCHER_TYPE_REGISTRY = ResourceKey.createRegistryKey(new ResourceLocation(MODID, "state_matcher_type"));

    Multiblock getMultiblock(ResourceLocation id);

    Codec<? extends StateMatcher> getStateMatcherCodec(ResourceLocation resourceLocation);

    Codec<? extends Multiblock> getMultiblockCodec(ResourceLocation resourceLocation);
}
