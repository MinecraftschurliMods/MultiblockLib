package com.github.minecraftschurlimods.multiblocklib.fabric.mixin;

import com.github.minecraftschurlimods.multiblocklib.fabric.xplat.FabricXplatAbstractions;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(PlayerList.class)
abstract class PlayerListMixin {
    @Shadow public abstract List<ServerPlayer> getPlayers();

    @Inject(method = "reloadResources", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;broadcastAll(Lnet/minecraft/network/protocol/Packet;)V"))
    private void onReloadResources(CallbackInfo ci) {
        for (final ServerPlayer player : getPlayers()) {
            FabricXplatAbstractions.reloadResources(player);
        }
    }

    @Inject(method = "placeNewPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;send(Lnet/minecraft/network/protocol/Packet;)V", ordinal = 5))
    private void onPlaceNewPlayer(Connection connection, ServerPlayer player, CallbackInfo ci) {
        FabricXplatAbstractions.reloadResources(player);
    }
}
