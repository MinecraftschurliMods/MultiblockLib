package com.github.minecraftschurlimods.multiblocklib.impl.matcher;

import com.github.minecraftschurlimods.multiblocklib.api.BlockStateMatchContext;
import com.github.minecraftschurlimods.multiblocklib.api.StateMatcher;
import com.github.minecraftschurlimods.multiblocklib.init.Init;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

public class BasicMatcher implements StateMatcher {
    public static final Codec<BasicMatcher> LOOSE_BLOCK = RecordCodecBuilder.create(inst -> inst.group(
            Registry.BLOCK.byNameCodec().fieldOf("block").forGetter(basicMatcher -> basicMatcher.state.getBlock())
    ).apply(inst, block -> new BasicMatcher(block.defaultBlockState(), false, Init.LOOSE_BLOCK_MATCHER.getId())));
    public static final Codec<BasicMatcher> STRICT_BLOCK = RecordCodecBuilder.create(inst -> inst.group(
            Registry.BLOCK.byNameCodec().fieldOf("block").forGetter(basicMatcher -> basicMatcher.state.getBlock())
    ).apply(inst, block -> new BasicMatcher(block.defaultBlockState(), true, Init.STRICT_BLOCK_MATCHER.getId())));
    public static final Codec<BasicMatcher> LOOSE_STATE = RecordCodecBuilder.create(inst -> inst.group(
            BlockState.CODEC.fieldOf("state").forGetter(basicMatcher -> basicMatcher.state)
    ).apply(inst, block -> new BasicMatcher(block, false, Init.LOOSE_STATE_MATCHER.getId())));
    public static final Codec<BasicMatcher> STRICT_STATE = RecordCodecBuilder.create(inst -> inst.group(
            BlockState.CODEC.fieldOf("state").forGetter(basicMatcher -> basicMatcher.state)
    ).apply(inst, block -> new BasicMatcher(block, true, Init.STRICT_STATE_MATCHER.getId())));

    private final BlockState state;
    private final boolean strict;
    private final ResourceLocation type;

    public BasicMatcher(BlockState state, boolean strict, ResourceLocation type) {
        this.state = state;
        this.strict = strict;
        this.type = type;
    }

    @Override
    public ResourceLocation getType() {
        return type;
    }

    @Override
    public BlockState displayState(final long gameTime) {
        return state;
    }

    @Override
    public boolean test(final BlockStateMatchContext context) {
        return strict ? context.state() == state : context.state().getBlock() == state.getBlock();
    }
}
