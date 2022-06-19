package com.github.minecraftschurlimods.multiblocklib.api;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import net.minecraft.core.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Interface representing a multiblock.
 */
public interface Multiblock {
    /**
     * The direct codec of a multiblock. It uses the type field to determine the actual codec to use for deserialization.
     * Register a multiblock type in the registry {@link MBAPI#MULTIBLOCK_TYPE_REGISTRY} to add your own type.
     */
    Codec<Multiblock> DIRECT_CODEC = MBAPI.INSTANCE.getMultiblockTypeRegistryCodec().dispatch(Multiblock::codec, Function.identity());

    /**
     * The codec of a multiblock reference, tries to get the entry from the registry.
     */
    Codec<Holder<Multiblock>> REFERENCE_CODEC = RegistryFileCodec.create(MBAPI.MULTIBLOCK_REGISTRY, DIRECT_CODEC);

    /**
     * Codec for a list of multiblocks, can be a tag, a list or a single element.
     */
    Codec<HolderSet<Multiblock>> LIST_CODEC = RegistryCodecs.homogeneousList(MBAPI.MULTIBLOCK_REGISTRY, DIRECT_CODEC);

    /**
     * @return The codec of this multiblock.
     */
    Codec<? extends Multiblock> codec();

    /**
     * @return Whether this multiblock is symmetrical.
     */
    boolean isSymmetrical();

    /**
     * @return The size of this multiblock.
     */
    Vec3i size();

    /**
     * Place this multiblock in the level provided anchored to the provided anchor position and using the supplied rotation and mirror.
     *
     * @param level     the level to place the multiblock in
     * @param anchorPos the position to place the multiblock at
     * @param rotation  the rotation of the multiblock
     * @param mirror    the mirror mode of the multiblock
     */
    void place(ServerLevel level, BlockPos anchorPos, Rotation rotation, Mirror mirror);

    /**
     * Check if this multiblock is present in the given level at the given anchor position with the given rotation and mirror mode.
     *
     * @param level     the level to check
     * @param anchorPos the position to check at
     * @param rotation  the rotation the multiblock should have
     * @param mirror    the mirror mode the multiblock should have
     * @return true if the multiblock is present, false otherwise
     */
    boolean matches(BlockGetter level, BlockPos anchorPos, Rotation rotation, Mirror mirror);

    /**
     * Get the rotation mode of this multiblock at the given position in the given level or null if it isn't present.
     *
     * @param level     the level to check in
     * @param anchorPos the position to check at
     * @return the rotation and mirror mode of the multiblock if it's present, or false otherwise
     */
    @Nullable Pair<Rotation, Mirror> matches(BlockGetter level, BlockPos anchorPos);

    /**
     * Simulate this multiblock at the given anchor position and with the given rotation and mirror mode.
     *
     * @param anchorPos the position to simulate this multiblock at
     * @param rotation  the rotation to simulate this multiblock with
     * @param mirror    the mirror mode to simulate this multiblock with
     * @return the simulation of this multiblock
     */
    default Collection<SimulateResult> simulate(BlockPos anchorPos, Rotation rotation, Mirror mirror) {
        return simulate(anchorPos, rotation, mirror, SimulateFilter.ALL);
    }

    /**
     * Simulate this multiblock at the given anchor position and with the given rotation and mirror mode.
     * Includes only the multiblock relative positions that match the given filter.
     *
     * @param anchorPos the position to simulate this multiblock at
     * @param rotation  the rotation to simulate this multiblock with
     * @param mirror    the mirror mode to simulate this multiblock with
     * @param filter     the filter to use
     * @return the simulation of this multiblock
     */
    Collection<SimulateResult> simulate(BlockPos anchorPos, Rotation rotation, Mirror mirror, SimulateFilter filter);

    /**
     * @return The name of this multiblock.
     */
    Component getName();

    /**
     * Interface representing a simulation result. A simulation result is a holder for a state matcher and its position in the world.
     */
    interface SimulateResult extends Predicate<BlockGetter> {
        /**
         * @return the position of this simulate result in the world
         */
        BlockPos worldPos();

        /**
         * @return the state matcher of this simulate result
         */
        StateMatcher stateMatcher();

        /**
         * @return the rotation of the simulate result
         */
        Rotation rotation();

        /**
         * @return the mirror mode of the simulate result
         */
        Mirror mirror();

        /**
         * Get the block state to display. Can be animated using the game time parameter.
         *
         * @param gameTime the game time
         * @return the display block state
         */
        BlockState displayState(long gameTime);

        /**
         * Check whether this simulate result is valid in the given level.
         *
         * @param level the level to check in
         * @return true if it matches, false otherwise
         */
        boolean test(BlockGetter level);
    }

    interface SimulateFilter extends Predicate<Vec3i> {
        SimulateFilter ALL = (pos) -> true;

        static SimulateFilter at(Vec3i pos) {
            return (p) -> p.equals(pos);
        }

        static SimulateFilter plane(int index, Direction.Axis axis) {
            return (p) -> p.get(axis) == index;
        }

        boolean test(Vec3i pos);
    }
}
