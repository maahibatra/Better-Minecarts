package io.github.maahibatra.betterminecarts;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.loader.impl.lib.sat4j.core.Vec;
import net.minecraft.block.*;
import net.minecraft.block.enums.RailShape;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.MinecartEntity;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.*;

// note: this is the first time i'm commenting in my code simply because when i fire up intellij the next day it feels like i just woke up stuck in a maze.

public class Betterminecarts implements ModInitializer {

    private final Map<UUID, Set<UUID>> linkMap = new HashMap<>();
    private final Map<UUID, Vec3d> lastPositions = new HashMap<>();
    private final Map<UUID, Long> lastUpdateTimes = new HashMap<>();

    @Override
    public void onInitialize() {
        System.out.println("BETTER MINECARTS LOADED SUCCESSFULLY.");

        HashMap<UUID, AbstractMinecartEntity> map = new HashMap<>();
        Set<List<UUID>> links = new HashSet<>();

        UseEntityCallback.EVENT.register((playerEntity, world, hand, entity, entityHitResult) -> {
            if (!world.isClient() && entity instanceof AbstractMinecartEntity) {
                if (playerEntity.getMainHandStack().getItem() != Items.CHAIN && entity instanceof MinecartEntity) {
                    playerEntity.sendMessage(Text.literal("sitting in a minecart, i see!"), false);
                    if(entity.getPassengerList().isEmpty()) {
                        playerEntity.startRiding(entity);
                    }
                    return ActionResult.SUCCESS;
                }

                BlockPos pos = entity.getBlockPos();
                Block block = world.getBlockState(pos).getBlock();

                if (!(block instanceof AbstractRailBlock)) {
                    playerEntity.sendMessage(Text.literal("minecart must be on rails."), false);
                    return ActionResult.SUCCESS;
                }

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
            long currTime = world.getServer().getTicks();
            Set<UUID> processed = new HashSet<>();
            for(Entity entity : world.iterateEntities()) { // entity iteration
                if(!(entity instanceof AbstractMinecartEntity cartA)) continue;

                // looks up linked minecarts
                UUID uuidA = cartA.getUuid();
                if(processed.contains(uuidA)) continue;
                Set<UUID> linkedUuids = linkMap.getOrDefault(uuidA, Collections.emptySet());

                for(UUID uuidB : linkedUuids) { // iterates over linked carts
                    Entity linked = world.getEntity(uuidB);
                    if(!(linked instanceof AbstractMinecartEntity cartB)) continue;
                    UUID uuidActualB = cartB.getUuid();
                    if(processed.contains(uuidActualB)) continue;

                    // skip if we alr processed the pair this tick
                    Long lastUpdateA = lastUpdateTimes.get(uuidA);
                    Long lastUpdateB = lastUpdateTimes.get(uuidActualB);
                    if((lastUpdateA != null && lastUpdateA == currTime) || (lastUpdateB != null && lastUpdateB == currTime)) continue;

                    // only process if both carts are on rails
                    BlockPos posA = cartA.getBlockPos();
                    BlockPos posB = cartB.getBlockPos();
                    if(!(world.getBlockState(posA).getBlock() instanceof AbstractRailBlock) || !(world.getBlockState(posB).getBlock() instanceof AbstractRailBlock)) continue;

                    // cart moving logic from this point on
                    Vec3d velA = cartA.getVelocity();
                    Vec3d velB = cartB.getVelocity();
                    Vec3d posVecA = cartA.getPos();
                    Vec3d posVecB = cartB.getPos();

                    // which cart is moving (faster)
                    Vec3d lastPosA = lastPositions.get(uuidA);
                    Vec3d lastPosB = lastPositions.get(uuidActualB);
                    double speedA = velA.length();
                    double speedB = velB.length();
                    double diff = Math.abs(speedA - speedB);

                    if(diff > 0.01 || (lastPosA != null && posVecA.subtract(lastPosA).length() > 0.05) || (lastPosB != null && posVecB.subtract(lastPosB).length() > 0.05)) {
                        AbstractMinecartEntity leader, follower;
                        Vec3d leaderPos, followerPos;
                        BlockPos leaderBPos, followerBPos;

                        // determine leader based and movement
                        boolean leadA = speedA > speedB + 0.01;
                        if(diff < 0.01 && lastPosA != null && lastPosB != null) {
                            double moveA = posVecA.subtract(lastPosA).length();
                            double moveB = posVecB.subtract(lastPosB).length();
                            leadA = moveA > moveB;
                        }

                        if(leadA) {
                            leader = cartA;
                            follower = cartB;
                            leaderPos = posVecA;
                            followerPos = posVecB;
                            leaderBPos = posA;
                            followerBPos = posB;
                        } else {
                            leader = cartB;
                            follower = cartA;
                            leaderPos = posVecB;
                            followerPos = posVecA;
                            leaderBPos = posB;
                            followerBPos = posA;
                        }

                        // calculate direction and adjust distance
                        Vec3d dir = leaderPos.subtract(followerPos);
                        double currDist = dir.length();

                        if(Math.abs(currDist - 2.0) > 0.1) {
                            if(currDist > 0.01) {
                                dir = dir.normalize();

                                BlockState leaderRail = world.getBlockState(leaderBPos);
//                                Vec3d railAwareDir = dir;
                                boolean corner = false;

                                if(leaderRail.getBlock() instanceof AbstractRailBlock railBlock) {
                                    RailShape shape = leaderRail.get(railBlock.getShapeProperty());
                                    corner = shape == RailShape.NORTH_EAST || shape == RailShape.NORTH_WEST || shape == RailShape.SOUTH_EAST || shape == RailShape.SOUTH_WEST;

//                                    Vec3d railDir;
//
//                                    switch (shape) {
//                                        case NORTH_SOUTH -> railDir = new Vec3d(0, 0, dir.z > 0 ? 1 : -1);
//                                        case EAST_WEST -> railDir = new Vec3d(dir.x > 0 ? 1 : -1, 0, 0);
//                                        case NORTH_EAST -> railDir = (dir.x > 0 && dir.z < 0) ? new Vec3d(0.707, 0, -0.707) : new Vec3d(-0.707, 0, 0.707);
//                                        case NORTH_WEST -> railDir = (dir.x < 0 && dir.z < 0) ? new Vec3d(-0.707, 0, -0.707) : new Vec3d(0.707, 0, 0.707);
//                                        case SOUTH_EAST -> railDir = (dir.x > 0 && dir.z > 0) ? new Vec3d(0.707, 0, 0.707) : new Vec3d(-0.707, 0, -0.707);
//                                        case SOUTH_WEST -> railDir = (dir.x < 0 && dir.z > 0) ? new Vec3d(-0.707, 0, 0.707) : new Vec3d(0.707, 0, -0.707);
//                                        case ASCENDING_NORTH -> railDir = new Vec3d(0, 0.5, -1).normalize();
//                                        case ASCENDING_SOUTH -> railDir = new Vec3d(0, 0.5, 1).normalize();
//                                        case ASCENDING_EAST -> railDir = new Vec3d(1, 0.5, 0).normalize();
//                                        case ASCENDING_WEST -> railDir = new Vec3d(-1, 0.5, 0).normalize();
//                                        default -> {
//                                            System.out.println("unhandled rail shape");
//                                            return;
//                                        }
//                                    }
//
//                                    if(railDir != null) {
//                                        railAwareDir = railDir;
//                                    }
//                                }

//                                Vec3d target = leaderPos.subtract(dir.multiply(2.0));
//                                Vec3d adjustment = target.subtract(followerPos).multiply(0.5);
//                                follower.setPosition(followerPos.add(adjustment));
                                }

                                Vec3d target;
                                if(corner && currDist > 1.8) {
                                    target = leaderPos.subtract(dir.multiply(1.8));
                                } else {
                                    target = leaderPos.subtract(dir.multiply(2.0));
                                }

                                BlockPos targetPos = BlockPos.ofFloored(target);
                                Block targetBlock = world.getBlockState(targetPos).getBlock();
                                Block targetBlockBelow = world.getBlockState(targetPos.down()).getBlock();

                                if(targetBlock instanceof AbstractRailBlock || targetBlockBelow instanceof AbstractRailBlock) {
                                    double factor = corner ? 0.3 : 0.5;
                                    Vec3d adjustment = target.subtract(followerPos).multiply(factor);
                                    follower.setPosition(followerPos.add(adjustment));
                                } else {
                                    Vec3d smallTarget = leaderPos.subtract(dir.multiply(1.5));
                                    Vec3d adjustment = smallTarget.subtract(followerPos).multiply(0.2);
                                    follower.setPosition(followerPos.add(adjustment));
                                }
                            }
                        }

                        // sync velocities
                        Vec3d leaderVel = leader.getVelocity();
                        if(leaderVel.length() > 0.01) {
                            follower.setVelocity(leaderVel);
                        }

                        lastUpdateTimes.put(uuidA, currTime);
                        lastUpdateTimes.put(uuidActualB, currTime);
                        processed.add(uuidA);
                        processed.add(uuidActualB);
                    }

                    lastPositions.put(uuidA, posVecA);
                    lastPositions.put(uuidActualB, posVecB);
                }
            }
        });
    }
}
