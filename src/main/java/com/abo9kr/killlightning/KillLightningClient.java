package com.abo9kr.killlightning;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;

public class KillLightningClient implements ClientModInitializer {

    private static final int WINDOW_TICKS = 100; // 5 ثواني

    private static PlayerEntity target;
    private static int tickCounter = 0;
    private static int lastHitTick = 0;
    private static int nextBoltId = -1000; // id سالب لتفادي التعارض مع كيانات السيرفر

    @Override
    public void onInitializeClient() {
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world.isClient && entity instanceof PlayerEntity victim && victim != player) {
                target = victim;
                lastHitTick = tickCounter;
            }
            return ActionResult.PASS;
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            tickCounter++;

            ClientWorld world = client.world;
            if (world == null || client.player == null) {
                target = null;
                return;
            }
            if (target == null) return;

            if (tickCounter - lastHitTick > WINDOW_TICKS) {
                target = null;
                return;
            }

            if (target.isDead()) {
                spawnBolt(world, target.getX(), target.getY(), target.getZ());
                target = null;
            }
        });
    }

    private static void spawnBolt(ClientWorld world, double x, double y, double z) {
        LightningEntity bolt = EntityType.LIGHTNING_BOLT.create(world);
        if (bolt == null) return;

        bolt.setId(nextBoltId--);
        bolt.refreshPositionAfterTeleport(x, y, z);
        bolt.setCosmetic(true); // شكلي: بدون حرق أو ضرر
        world.addEntity(bolt);  // الصوت والوميض يشتغلان تلقائياً عند الكلاينت
    }
}
