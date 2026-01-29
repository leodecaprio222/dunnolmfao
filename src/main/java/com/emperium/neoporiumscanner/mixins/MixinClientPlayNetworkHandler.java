package com.emperium.neoporiumscanner.mixins;

import com.emperium.neoporiumscanner.core.ScanController;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.network.packet.s2c.play.UnloadChunkS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class MixinClientPlayNetworkHandler {

    @Inject(method = "onChunkData", at = @At("TAIL"))
    private void onChunkData(ChunkDataS2CPacket packet, CallbackInfo ci) {
        // When a chunk is loaded, we might want to scan it
        ScanController.getInstance().onChunkLoaded(packet.getChunkX(), packet.getChunkZ());
    }

    @Inject(method = "onUnloadChunk", at = @At("TAIL"))
    private void onUnloadChunk(UnloadChunkS2CPacket packet, CallbackInfo ci) {
        // When a chunk is unloaded, clear its data from cache
        ScanController.getInstance().onChunkUnloaded(packet.getChunkX(), packet.getChunkZ());
    }

    @Inject(method = "onBlockUpdate", at = @At("TAIL"))
    private void onBlockUpdate(BlockUpdateS2CPacket packet, CallbackInfo ci) {
        // When a block updates, check if it affects our scanning
        ScanController.getInstance().onBlockUpdated(packet.getPos());
    }
}