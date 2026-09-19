package com.abo9kr.killlightning;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;

public class KillLightning implements ModInitializer {

    public static final String MOD_ID = "killlightning";

    @Override
    public void onInitialize() {
        ServerEntityCombatEvents.AFTER_KILLED_OTHER_ENTITY.register((world, entity, killedEntity) -> {
            if (!(entity instanceof ServerPlayerEntity killer)) return;
            if (!(killedEntity instanceof PlayerEntity) || killer == killedEntity) return;

            LightningEntity bolt = EntityType.LIGHTNING_BOLT.create(world);
            if (bolt == null) return;

            bolt.refreshPositionAfterTeleport(
                    killedEntity.getX(), killedEntity.getY(), killedEntity.getZ());

            // يُرسل للقاتل فقط، ولا يُضاف للعالم فلا حرق ولا ضرر
            killer.networkHandler.sendPacket(
                    new EntitySpawnS2CPacket(bolt, 0, bolt.getBlockPos()));
        });
    }
}
