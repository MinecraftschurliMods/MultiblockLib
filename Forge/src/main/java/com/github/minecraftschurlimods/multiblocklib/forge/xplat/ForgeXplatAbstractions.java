package com.github.minecraftschurlimods.multiblocklib.forge.xplat;

import com.github.minecraftschurlimods.multiblocklib.api.MBAPI;
import com.github.minecraftschurlimods.multiblocklib.api.Multiblock;
import com.github.minecraftschurlimods.multiblocklib.api.StateMatcher;
import com.github.minecraftschurlimods.multiblocklib.xplat.XplatAbstractions;
import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.NewRegistryEvent;
import net.minecraftforge.registries.RegistryBuilder;

import java.util.function.Supplier;

public class ForgeXplatAbstractions implements XplatAbstractions {

    private Supplier<IForgeRegistry<Codec<? extends Multiblock>>> multiblockTypes;
    private Supplier<IForgeRegistry<Codec<? extends StateMatcher>>> stateMatcherTypes;
    private final Codec<Codec<? extends Multiblock>> multiblockTypeCodec = ExtraCodecs.lazyInitializedCodec(() -> multiblockTypes.get().getCodec());
    private final Codec<Codec<? extends StateMatcher>> stateMatcherTypeCodec = ExtraCodecs.lazyInitializedCodec(() -> stateMatcherTypes.get().getCodec());

    @Override
    public void init() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener((NewRegistryEvent event) -> {
            multiblockTypes = event.create(new RegistryBuilder<Codec<? extends Multiblock>>().setName(MBAPI.MULTIBLOCK_TYPE_REGISTRY.location()).setDefaultKey(new ResourceLocation(MBAPI.MODID, "dense")));
            stateMatcherTypes = event.create(new RegistryBuilder<Codec<? extends StateMatcher>>().setName(MBAPI.STATE_MATCHER_TYPE_REGISTRY.location()).setDefaultKey(new ResourceLocation(MBAPI.MODID, "strict_state")));
            event.create(new RegistryBuilder<Multiblock>().setName(MBAPI.MULTIBLOCK_REGISTRY.location()).dataPackRegistry(Multiblock.DIRECT_CODEC));
        });
    }

    @Override
    public Codec<Codec<? extends Multiblock>> getMultiblockTypeRegistryCodec() {
        return multiblockTypeCodec;
    }

    @Override
    public Codec<Codec<? extends StateMatcher>> getStateMatcherTypeRegistryCodec() {
        return stateMatcherTypeCodec;
    }
}
