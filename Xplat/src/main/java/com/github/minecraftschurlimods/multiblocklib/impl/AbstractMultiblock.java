package com.github.minecraftschurlimods.multiblocklib.impl;

import com.github.minecraftschurlimods.multiblocklib.api.BlockStateMatchContext;
import com.github.minecraftschurlimods.multiblocklib.api.MBAPI;
import com.github.minecraftschurlimods.multiblocklib.api.Multiblock;
import com.github.minecraftschurlimods.multiblocklib.api.StateMatcher;
import com.github.minecraftschurlimods.multiblocklib.api.matcher.AirMatcher;
import com.github.minecraftschurlimods.multiblocklib.api.matcher.AnyMatcher;
import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractMultiblock implements Multiblock {
    protected final Vec3i size;
    protected final boolean symmetrical;

    public AbstractMultiblock(boolean symmetrical, Vec3i size) {
        this.size = size;
        this.symmetrical = symmetrical;
    }

    @Override
    public boolean isSymmetrical() {
        return symmetrical;
    }

    @Override
    public Vec3i size() {
        return size;
    }

    @Override
    public void place(ServerLevel level, BlockPos anchorPos, Rotation rotation, Mirror mirror) {
        simulate(anchorPos, rotation, mirror, SimulateFilter.ALL).forEach(r -> {
            BlockPos placePos = r.worldPos();
            BlockState targetState = r.displayState(level.getGameTime());
            if (!targetState.isAir() && targetState.canSurvive(level, placePos) && level.getBlockState(placePos).getMaterial().isReplaceable()) {
                level.setBlockAndUpdate(placePos, targetState);
            }
        });
    }

    @Override
    public boolean matches(BlockGetter level, BlockPos anchorPos, Rotation rotation, Mirror mirror) {
        return simulate(anchorPos, rotation, mirror, SimulateFilter.ALL).stream().allMatch(simulateResult -> simulateResult.stateMatcher().test(new DenseMultiblock.SimpleBlockStateMatchContext(simulateResult.worldPos(), level, rotation, mirror)));
    }

    @Nullable
    @Override
    public Pair<Rotation, Mirror> matches(BlockGetter level, BlockPos anchorPos) {
        if (isSymmetrical() && matches(level, anchorPos, Rotation.NONE, Mirror.NONE)) {
            return Pair.of(Rotation.NONE, Mirror.NONE);
        } else {
            for (Rotation rotation : Rotation.values()) {
                for (Mirror mirror : Mirror.values()) {
                    if (matches(level, anchorPos, rotation, mirror)) {
                        return Pair.of(rotation, mirror);
                    }
                }
            }
        }
        return null;
    }

    private static final Map<Character, StateMatcher> DEFAULT_MATCHERS = Util.make(new HashMap<>(), map -> {
        map.put('_', new AnyMatcher());
        map.put(' ', new AirMatcher());
        map.put('0', new AirMatcher());
    });

    protected static Map<Character, StateMatcher> addDefaultMappings(Map<Character, StateMatcher> mapping) {
        var builder = ImmutableMap.<Character, StateMatcher>builder();
        builder.putAll(mapping);
        for (final Map.Entry<Character, StateMatcher> entry : DEFAULT_MATCHERS.entrySet()) {
            if (!mapping.containsKey(entry.getKey())) {
                builder.put(entry.getKey(), entry.getValue());
            }
        }
        return builder.build();
    }

    protected static Map<Character, StateMatcher> removeDefaultMappings(Map<Character, StateMatcher> mapping) {
        Map<Character, StateMatcher> out = new HashMap<>(mapping);
        for (Map.Entry<Character, StateMatcher> entry : DEFAULT_MATCHERS.entrySet()) {
            if (mapping.containsKey(entry.getKey()) && mapping.get(entry.getKey()) == entry.getValue()) {
                out.remove(entry.getKey());
            }
        }
        return out;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AbstractMultiblock that = (AbstractMultiblock) o;

        if (isSymmetrical() != that.isSymmetrical()) {
            return false;
        }
        if (!size.equals(that.size)) {
            return false;
        }
        return this.equals(that);
    }

    public abstract boolean equals(AbstractMultiblock other);

    @Override
    public abstract int hashCode();

    @Override
    public Component getName() {
        return Component.translatable(Util.makeDescriptionId("multiblock", MBAPI.INSTANCE.getMultiblockId(this)));
    }

    protected record SimulateResultImpl(StateMatcher stateMatcher, BlockPos worldPos, Rotation rotation, Mirror mirror) implements SimulateResult {
        @Override
        public BlockState displayState(final long gameTime) {
            return stateMatcher().displayState(gameTime).rotate(rotation()).mirror(mirror());
        }

        @Override
        public boolean test(final BlockGetter blockGetter) {
            return stateMatcher().test(new SimpleBlockStateMatchContext(worldPos(), blockGetter, rotation(), mirror()));
        }
    }

    static class SimpleBlockStateMatchContext implements BlockStateMatchContext {
        private final Map<String, Object> data = new HashMap<>();
        private final BlockPos pos;
        private final BlockGetter level;
        private final Rotation rotation;
        private final Mirror mirror;
        private @Nullable BlockState state;
        private @Nullable FluidState fluidState;
        private @Nullable BlockEntity blockEntity;

        public SimpleBlockStateMatchContext(BlockPos pos, BlockGetter level, Rotation rotation, Mirror mirror) {
            this.pos = pos;
            this.level = level;
            this.rotation = switch (rotation) {
                case NONE -> Rotation.NONE;
                case CLOCKWISE_90 -> Rotation.COUNTERCLOCKWISE_90;
                case CLOCKWISE_180 -> Rotation.CLOCKWISE_180;
                case COUNTERCLOCKWISE_90 -> Rotation.CLOCKWISE_90;
            };
            this.mirror = mirror;
            /*mirror = switch (mirror) {
                case LEFT_RIGHT -> Mirror.NONE;
                case FRONT_BACK -> Mirror.FRONT_BACK;
                case NONE -> Mirror.LEFT_RIGHT;
            };*/
        }

        @Override
        public BlockPos pos() {
            return pos;
        }

        @Override
        public BlockState state() {
            if (state == null) {
                this.state = level.getBlockState(pos).mirror(mirror).rotate(rotation);
            }
            return state;
        }

        @Override
        public FluidState fluidState() {
            if (fluidState == null) {
                this.fluidState = level.getFluidState(pos);
            }
            return fluidState;
        }

        @Override
        @Nullable
        public BlockEntity blockEntity() {
            if (blockEntity == null) {
                this.blockEntity = level.getBlockEntity(pos);
            }
            return blockEntity;
        }

        @Override
        public BlockGetter level() {
            return level;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> T getAdditionalData(final String key) {
            return (T) data.get(key);
        }

        @Override
        public <T> void withAdditionalData(final String key, final T value) {
            data.put(key, value);
        }
    }
}
