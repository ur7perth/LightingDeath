package com.abo9kr.killlightning;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;

public class KillLightningClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(LightningPayload.ID, (payload, context) -> {
            ClientWorld world = context.client().world;
            if (world == null) return;

            LightningEntity bolt = EntityType.LIGHTNING_BOLT.create(world);
            if (bolt == null) return;

            bolt.refreshPositionAfterTeleport(payload.x(), payload.y(), payload.z());
            bolt.setCosmetic(true); // شكلي: بدون حرق أو ضرر
            world.addEntity(bolt);
        });
    }
}
