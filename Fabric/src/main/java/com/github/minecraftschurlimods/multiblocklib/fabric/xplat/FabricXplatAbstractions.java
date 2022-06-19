package com.github.minecraftschurlimods.multiblocklib.fabric.xplat;

import com.github.minecraftschurlimods.multiblocklib.api.MBAPI;
import com.github.minecraftschurlimods.multiblocklib.api.Multiblock;
import com.github.minecraftschurlimods.multiblocklib.api.StateMatcher;
import com.github.minecraftschurlimods.multiblocklib.init.Init;
import com.github.minecraftschurlimods.multiblocklib.xplat.XplatAbstractions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;

public class FabricXplatAbstractions implements XplatAbstractions {
    private Registry<Codec<? extends Multiblock>> multiblockTypeRegistry;
    private Registry<Codec<? extends StateMatcher>> stateMatcherTypeRegistry;

    @Override
    public void init() {
        multiblockTypeRegistry = FabricRegistryBuilder.createDefaulted((Class<Codec<? extends Multiblock>>)((Object)Codec.class), MBAPI.MULTIBLOCK_TYPE_REGISTRY.location(), new ResourceLocation(MBAPI.MODID, "dense")).buildAndRegister();
        stateMatcherTypeRegistry = FabricRegistryBuilder.createDefaulted((Class<Codec<? extends StateMatcher>>)((Object)Codec.class), MBAPI.STATE_MATCHER_TYPE_REGISTRY.location(), new ResourceLocation(MBAPI.MODID, "strict_state")).buildAndRegister();
        createRegistry(MBAPI.MULTIBLOCK_REGISTRY, Lifecycle.experimental());
        ((FabricRegistryProviderFactory.Provider<Codec<? extends Multiblock>>) Init.MULTIBLOCK_TYPES).register();
        ((FabricRegistryProviderFactory.Provider<Codec<? extends StateMatcher>>) Init.STATE_MATCHER_TYPES).register();
    }

    private <T> Registry<T> createRegistry(ResourceKey<Registry<T>> registryKey, Lifecycle lifecycle) {
        MappedRegistry<T> registry = new MappedRegistry<>(registryKey, lifecycle, null);
        ((WritableRegistry) BuiltinRegistries.REGISTRY).register(registryKey, registry, lifecycle);
        return registry;
    }

    @Override
    public Codec<Codec<? extends Multiblock>> getMultiblockTypeRegistryCodec() {
        return ExtraCodecs.lazyInitializedCodec(() -> multiblockTypeRegistry.byNameCodec());
    }

    @Override
    public Codec<Codec<? extends StateMatcher>> getStateMatcherTypeRegistryCodec() {
        return ExtraCodecs.lazyInitializedCodec(() -> stateMatcherTypeRegistry.byNameCodec());
    }
}
