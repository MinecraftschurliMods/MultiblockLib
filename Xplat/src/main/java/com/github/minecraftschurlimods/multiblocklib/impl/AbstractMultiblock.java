package com.github.minecraftschurlimods.multiblocklib.impl;

import com.github.minecraftschurlimods.multiblocklib.api.BlockStateMatchContext;
import com.github.minecraftschurlimods.multiblocklib.api.Multiblock;
import com.github.minecraftschurlimods.multiblocklib.api.StateMatcher;
import com.github.minecraftschurlimods.multiblocklib.impl.matcher.AirMatcher;
import com.github.minecraftschurlimods.multiblocklib.impl.matcher.AnyMatcher;
import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
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
    public void place(final Level level, final BlockPos anchorPos, final Rotation rotation, final Mirror mirror) {
        simulate(anchorPos, rotation, mirror).forEach(r -> {
            BlockPos placePos = r.worldPos();
            BlockState targetState = r.displayState(level.getGameTime());
            if (!targetState.isAir() && targetState.canSurvive(level, placePos) && level.getBlockState(placePos).getMaterial().isReplaceable()) {
                level.setBlockAndUpdate(placePos, targetState);
            }
        });
    }

    @Override
    public boolean matches(final BlockGetter level, final BlockPos anchorPos, final Rotation rotation, final Mirror mirror) {
        return simulate(anchorPos, rotation, mirror).stream().allMatch(simulateResult -> simulateResult.stateMatcher().test(new DenseMultiblock.SimpleBlockStateMatchContext(simulateResult.worldPos(), level, rotation, mirror)));
    }

    @Nullable
    @Override
    public Pair<Rotation, Mirror> matches(final BlockGetter level, final BlockPos anchorPos) {
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

    protected static Map<Character, StateMatcher> addDefaultMappings(final Map<Character, StateMatcher> mapping) {
        var builder = ImmutableMap.<Character, StateMatcher>builder();
        builder.putAll(mapping);
        for (final Map.Entry<Character, StateMatcher> entry : DEFAULT_MATCHERS.entrySet()) {
            if (!mapping.containsKey(entry.getKey())) {
                builder.put(entry.getKey(), entry.getValue());
            }
        }
        return builder.build();
    }

    protected static Map<Character, StateMatcher> removeDefaultMappings(final Map<Character, StateMatcher> mapping) {
        Map<Character, StateMatcher> out = new HashMap<>(mapping);
        for (final Map.Entry<Character, StateMatcher> entry : DEFAULT_MATCHERS.entrySet()) {
            if (mapping.containsKey(entry.getKey()) && mapping.get(entry.getKey()) == entry.getValue()) {
                out.remove(entry.getKey());
            }
        }
        return out;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final AbstractMultiblock that = (AbstractMultiblock) o;

        if (isSymmetrical() != that.isSymmetrical()) {
            return false;
        }
        if (!size.equals(that.size)) {
            return false;
        }
        return this.equals(that);
    }

    public abstract boolean equals(final AbstractMultiblock other);

    public abstract int hashCode();

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
        private final BlockPos pos;
        private final BlockGetter level;
        private final BlockState state;
        private final Map<String, Object> data = new HashMap<>();

        public SimpleBlockStateMatchContext(final BlockPos pos, final BlockGetter level, Rotation rotation, Mirror mirror) {
            this.pos = pos;
            this.level = level;
            /*mirror = switch (mirror) {
                case LEFT_RIGHT -> Mirror.NONE;
                case FRONT_BACK -> Mirror.FRONT_BACK;
                case NONE -> Mirror.LEFT_RIGHT;
            };*/
            rotation = switch (rotation) {
                case NONE -> Rotation.NONE;
                case CLOCKWISE_90 -> Rotation.COUNTERCLOCKWISE_90;
                case CLOCKWISE_180 -> Rotation.CLOCKWISE_180;
                case COUNTERCLOCKWISE_90 -> Rotation.CLOCKWISE_90;
            };
            this.state = level.getBlockState(pos).mirror(mirror).rotate(rotation);
        }

        @Override
        public BlockPos pos() {
            return pos;
        }

        @Override
        public BlockState state() {
            return state;
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
