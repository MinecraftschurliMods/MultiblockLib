package com.github.minecraftschurlimods.multiblocklib.api;

import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Function;

public interface StateMatcher {
    Codec<StateMatcher> CODEC = MBAPI.INSTANCE.getStateMatcherTypeRegistryCodec().dispatch(StateMatcher::codec, Function.identity());

    Codec<? extends StateMatcher> codec();
    BlockState displayState(final long gameTime);
    boolean test(BlockStateMatchContext context);
    default boolean isAir() {
        return false;
    }
}
