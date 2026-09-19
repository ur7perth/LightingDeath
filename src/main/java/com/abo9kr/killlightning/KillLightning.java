package com.abo9kr.killlightning;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;

public class KillLightning implements ModInitializer {

    public static final String MOD_ID = "killlightning";

    @Override
    public void onInitialize() {
        PayloadTypeRegistry.playS2C().register(LightningPayload.ID, LightningPayload.CODEC);

        ServerEntityCombatEvents.AFTER_KILLED_OTHER_ENTITY.register((world, entity, killedEntity) -> {
            if (entity instanceof ServerPlayerEntity killer
                    && killedEntity instanceof PlayerEntity
                    && killer != killedEntity) {
                ServerPlayNetworking.send(killer, new LightningPayload(
                        killedEntity.getX(), killedEntity.getY(), killedEntity.getZ()));
            }
        });
    }
}
