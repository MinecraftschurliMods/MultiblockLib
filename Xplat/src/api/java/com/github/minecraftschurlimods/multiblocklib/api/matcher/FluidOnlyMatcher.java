package com.github.minecraftschurlimods.multiblocklib.api.matcher;

import com.github.minecraftschurlimods.multiblocklib.api.BlockStateMatchContext;
import com.github.minecraftschurlimods.multiblocklib.api.StateMatcher;
import com.mojang.serialization.Codec;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;

public record FluidOnlyMatcher(HolderSet<Fluid> tag) implements StateMatcher {
    public static final Codec<FluidOnlyMatcher> CODEC = RegistryCodecs.homogeneousList(Registry.FLUID_REGISTRY).fieldOf("fluid").xmap(FluidOnlyMatcher::new, FluidOnlyMatcher::tag).codec();

    @Override
    public Codec<? extends StateMatcher> codec() {
        return CODEC;
    }

    @Override
    public BlockState displayState(long gameTime) {
        int index = (int) (gameTime / 20 % tag().size());
        return tag().get(index).value().defaultFluidState().createLegacyBlock();
    }

    @Override
    public boolean test(BlockStateMatchContext context) {
        return context.fluidState().is(tag());
    }
}
