package com.github.minecraftschurlimods.multiblocklib.fabric.mixin;

import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//TODO @Mixin(PlayerList.class)
abstract class PlayerListMixin {
    //TODO @Inject(method = "reloadResources", at = @At(value = "INVOKE"))
    private void onReloadResources(CallbackInfo ci) {}
    //TODO @Inject(method = "placeNewPlayer", at = @At(value = "INVOKE"))
    private void onPlaceNewPlayer(Connection connection, ServerPlayer player, CallbackInfo ci) {}
}
