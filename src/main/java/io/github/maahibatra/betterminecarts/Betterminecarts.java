package io.github.maahibatra.betterminecarts;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.loader.impl.lib.sat4j.core.Vec;
import net.minecraft.block.BlockState;
import net.minecraft.block.RailBlock;
import net.minecraft.block.enums.RailShape;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.MinecartEntity;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.Block;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.*;

// note: this is the first time i'm commenting in my code simply because when i fire up intellij the next day it feels like i just woke up stuck in a maze.

public class Betterminecarts implements ModInitializer {

    private final Map<UUID, Set<UUID>> linkMap = new HashMap<>();
    private final Map<UUID, Vec3d> lastPositions = new HashMap<>();

    @Override
    public void onInitialize() {
        System.out.println("BETTER MINECARTS LOADED SUCCESSFULLY.");

        HashMap<UUID, AbstractMinecartEntity> map = new HashMap<>();
        Set<List<UUID>> links = new HashSet<>();

        UseEntityCallback.EVENT.register((playerEntity, world, hand, entity, entityHitResult) -> {
            if (!world.isClient() && entity.getType() == EntityType.MINECART) {
                if (playerEntity.getMainHandStack().getItem() != Items.CHAIN) {
                    playerEntity.sendMessage(Text.literal("sitting in a minecart, i see!"), false);
                    if(entity.getPassengerList().isEmpty()) {
                        playerEntity.startRiding(entity);
                    }
                    return ActionResult.SUCCESS;
                }

                BlockPos pos = entity.getBlockPos();
                Block block = world.getBlockState(pos).getBlock();

                if (!(block instanceof RailBlock)) {
                    playerEntity.sendMessage(Text.literal("minecart must be on rails."), false);
                    return ActionResult.SUCCESS;
                } // make this into same railway track instead

                if(map.isEmpty()) {
                    playerEntity.sendMessage(Text.literal("you clicked the first minecart with a chain!"), false);
                    map.put(playerEntity.getUuid(), (MinecartEntity) entity);
                } else {
                    AbstractMinecartEntity storedCart = map.get(playerEntity.getUuid());

                    if (storedCart == null || !storedCart.isAlive() || storedCart.isRemoved() || storedCart == entity) {
                        playerEntity.sendMessage(Text.literal("the first minecart is invalid. this is your first minecart, now."), false);
                        map.put(playerEntity.getUuid(), (MinecartEntity) entity);
                    } else {
                        BlockPos pos1 = storedCart.getBlockPos();
                        BlockPos pos2 = entity.getBlockPos();

                        if(pos1.getManhattanDistance(pos2) != 2) {
                            playerEntity.sendMessage(Text.literal("minecarts must be exactly one block apart."), false);
                            map.remove(playerEntity.getUuid());
                            return ActionResult.SUCCESS;
                        }

                        UUID uuid1 = storedCart.getUuid();
                        UUID uuid2 = entity.getUuid();

                        List<UUID> pair = Arrays.asList(uuid1, uuid2);
                        pair.sort(Comparator.naturalOrder());

                        if(links.contains(pair)) {
                            playerEntity.sendMessage(Text.literal("these minecarts are already linked."), false);
                        } else if(linkMap.getOrDefault(uuid1, Collections.emptySet()).size() >= 2 || linkMap.getOrDefault(uuid2, Collections.emptySet()).size() >= 2) {
                            playerEntity.sendMessage(Text.literal("one of these minecarts already has more than two links and cannot be linked to more."), false);
                        } else {
                            links.add(pair);
                            playerEntity.sendMessage(Text.literal("you clicked the second minecart and linked it!"), false);
                            playerEntity.getMainHandStack().decrement(1);

                            linkMap.computeIfAbsent(uuid1, k -> new HashSet<>()).add(uuid2);
                            linkMap.computeIfAbsent(uuid2, k -> new HashSet<>()).add(uuid1);
                        }

                        map.remove(playerEntity.getUuid());
                    }
                }
            }
            return ActionResult.SUCCESS;
        });

        ServerTickEvents.END_WORLD_TICK.register(world -> {
            for(Entity entity : world.iterateEntities()) { // entity iteration
                if(!(entity instanceof AbstractMinecartEntity cartA)) continue;

                // looks up linked minecarts
                UUID uuidA = cartA.getUuid();
                Set<UUID> linkedUuids = linkMap.getOrDefault(uuidA, Collections.emptySet());

                for(UUID uuidB : linkedUuids) { // iterates over linked carts
                    Entity linked = world.getEntity(uuidB);
                    if (!(linked instanceof AbstractMinecartEntity cartB)) continue;

                    // minecarts move together
                    Vec3d velA = cartA.getVelocity();
                    Vec3d velB = cartB.getVelocity();

                    if(velA.subtract(velB).length() > 0.01) { // vel diff present
                        if (velA.length() > velB.length()) {
                            System.out.println("carts should move in direction of cartA");
                            cartB.setVelocity(velA);

                            Vec3d dirA = cartA.getPos().subtract(cartB.getPos()).normalize();
                            Vec3d posB = cartA.getPos().subtract(dirA.multiply(2.0));
                            cartB.setPosition(posB);
                        } else {
                            System.out.println("carts should move in direction of cartB");
                            cartA.setVelocity(velB);
                            
                            Vec3d dirB = cartB.getPos().subtract(cartA.getPos()).normalize();
                            Vec3d posA = cartB.getPos().subtract(dirB.multiply(2.0));
                            cartA.setPosition(posA);
                        }
                    }
                }
            }
        });
    }
}
