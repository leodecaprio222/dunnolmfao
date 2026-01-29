package com.emperium.neoporiumscanner.mixins;

import com.emperium.neoporiumscanner.xray.XRayRenderer;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class MixinClientPlayNetworkHandler {
    @Inject(method = "onBlockUpdate", at = @At("RETURN"))
    private void onBlockUpdate(BlockUpdateS2CPacket packet, CallbackInfo ci) {
        // Clear render cache when blocks update
        XRayRenderer.clearBlocks();
    }
}