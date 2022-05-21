package com.github.minecraftschurlimods.multiblocklib.impl.matcher;

import com.github.minecraftschurlimods.multiblocklib.Util;
import com.github.minecraftschurlimods.multiblocklib.api.BlockStateMatchContext;
import com.github.minecraftschurlimods.multiblocklib.api.StateMatcher;
import com.github.minecraftschurlimods.multiblocklib.init.Init;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

public class DisplayOnlyMatcher implements StateMatcher {
    public static final Codec<DisplayOnlyMatcher> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Util.BLOCK_STATE_CODEC.fieldOf("display").forGetter(displayOnlyMatcher -> displayOnlyMatcher.displayState)
    ).apply(inst, DisplayOnlyMatcher::new));

    private final BlockState displayState;

    public DisplayOnlyMatcher(BlockState displayState) {
        this.displayState = displayState;
    }

    @Override
    public ResourceLocation getType() {
        return Init.DISPLAY_ONLY_MATCHER.getId();
    }

    @Override
    public BlockState displayState(final long gameTime) {
        return displayState;
    }

    @Override
    public boolean test(final BlockStateMatchContext context) {
        return true;
    }
}
