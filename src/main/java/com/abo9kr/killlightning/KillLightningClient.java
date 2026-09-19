package com.abo9kr.killlightning;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class KillLightningClient implements ClientModInitializer {

    private static final boolean DEBUG = false;
    private static final int WINDOW_TICKS = 100;       // 5 ثواني بعد آخر ضربة
    private static final int BOLT_LIFETIME_TICKS = 30; // 1.5 ثانية ثم يُحذف

    private static PlayerEntity target;
    private static String targetName;
    private static double lastX, lastY, lastZ;
    private static int tickCounter = 0;
    private static int lastHitTick = 0;
    private static int nextBoltId = -1000;

    private static final List<ActiveBolt> bolts = new ArrayList<>();

    private record ActiveBolt(LightningEntity entity, int expireTick) {}

    @Override
    public void onInitializeClient() {
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world.isClient && entity instanceof PlayerEntity victim && victim != player) {
                target = victim;
                targetName = victim.getName().getString();
                lastX = victim.getX();
                lastY = victim.getY();
                lastZ = victim.getZ();
                lastHitTick = tickCounter;
                debug("hit " + targetName);
            }
            return ActionResult.PASS;
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            tickCounter++;

            ClientWorld world = client.world;
            if (world == null || client.player == null) {
                target = null;
                bolts.clear();
                return;
            }

            cleanupBolts(world);

            if (target == null) return;

            if (tickCounter - lastHitTick > WINDOW_TICKS) {
                target = null;
                return;
            }

            if (target.isDead()) {
                trigger(client, "health 0");
                return;
            }

            if (target.isRemoved()) {
                double dx = client.player.getX() - lastX;
                double dz = client.player.getZ() - lastZ;
                if (dx * dx + dz * dz < 100) {
                    trigger(client, "removed");
                } else {
                    target = null;
                }
                return;
            }

            // نحدّث آخر موقع معروف للاعب
            lastX = target.getX();
            lastY = target.getY();
            lastZ = target.getZ();
        });

        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (overlay || target == null || targetName == null) return;
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null) return;

            String text = message.getString();
            String me = client.player.getName().getString();
            if (text.contains(targetName) && text.contains(me)
                    && tickCounter - lastHitTick <= WINDOW_TICKS) {
                trigger(client, "chat");
            }
        });
    }

    private static void trigger(MinecraftClient client, String reason) {
        ClientWorld world = client.world;
        if (world != null) {
            spawnBolt(world, lastX, lastY, lastZ);
            debug("lightning (" + reason + ")");
        }
        target = null;
        targetName = null;
    }

    private static void spawnBolt(ClientWorld world, double x, double y, double z) {
        LightningEntity bolt = EntityType.LIGHTNING_BOLT.create(world);
        if (bolt == null) return;

        bolt.setId(nextBoltId--);
        bolt.refreshPositionAfterTeleport(x, y, z);
        bolt.setCosmetic(true); // بدون حرق أو ضرر
        world.addEntity(bolt);

        bolts.add(new ActiveBolt(bolt, tickCounter + BOLT_LIFETIME_TICKS));
    }

    // يحذف البرق عند انتهائه أو عند تجاوز مدته
    private static void cleanupBolts(ClientWorld world) {
        Iterator<ActiveBolt> it = bolts.iterator();
        while (it.hasNext()) {
            ActiveBolt b = it.next();
            if (b.entity().isRemoved()) {
                it.remove();
            } else if (tickCounter >= b.expireTick()) {
                world.removeEntity(b.entity().getId(), Entity.RemovalReason.DISCARDED);
                it.remove();
            }
        }
    }

    private static void debug(String msg) {
        if (!DEBUG) return;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.sendMessage(Text.literal("[KL] " + msg), false);
        }
    }
}
