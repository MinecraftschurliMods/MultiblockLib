package com.github.minecraftschurlimods.multiblocklib.impl.matcher;

import com.github.minecraftschurlimods.multiblocklib.init.Init;
import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;

public class AnyMatcher extends DisplayOnlyMatcher {
    public static final Codec<AnyMatcher> CODEC = Codec.unit(AnyMatcher::new);

    public AnyMatcher() {
        super(Blocks.AIR.defaultBlockState());
    }

    @Override
    public ResourceLocation getType() {
        return Init.ANY_MATCHER.getId();
    }
}
