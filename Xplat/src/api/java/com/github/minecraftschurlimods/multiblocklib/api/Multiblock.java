package com.github.minecraftschurlimods.multiblocklib.api;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.function.Predicate;

/**
 * Interface representing a multiblock.
 */
public interface Multiblock {
    /**
     * The codec of a multiblock. It uses the type field to determine the actual codec to use.
     * Register a multiblock type in the registry {@link MBAPI#MULTIBLOCK_TYPE_REGISTRY} to add your own type.
     */
    Codec<Multiblock> CODEC = ResourceLocation.CODEC.dispatch(Multiblock::getType, MBAPI.INSTANCE::getMultiblockCodec);

    /**
     * @return The type of this multiblock.
     */
    ResourceLocation getType();

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
    void place(Level level, BlockPos anchorPos, Rotation rotation, Mirror mirror);

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
    Collection<SimulateResult> simulate(BlockPos anchorPos, Rotation rotation, Mirror mirror);

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
}
