package io.github.maahibatra.betterminecarts;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.block.RailBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.MinecartEntity;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.Block;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public class Betterminecarts implements ModInitializer {

    @Override
    public void onInitialize() {
        System.out.println("BETTER MINECARTS LOADED SUCCESSFULLY.");

        HashMap<UUID, AbstractMinecartEntity> map = new HashMap<>();

        UseEntityCallback.EVENT.register((playerEntity, world, hand, entity, entityHitResult) -> {
            if(!world.isClient() && entity.getType() == EntityType.MINECART && playerEntity.getMainHandStack().getItem() == Items.CHAIN) {

                BlockPos pos = entity.getBlockPos();
                Block block = world.getBlockState(pos).getBlock();

                if (!(block instanceof RailBlock)) {
                    playerEntity.sendMessage(Text.literal("Minecart must be on rails."), false);
                    return ActionResult.SUCCESS;
                }

                if(map.size() == 0) {
                    playerEntity.sendMessage(Text.literal("You clicked the first minecart with a chain!"), false);
                    map.put(playerEntity.getUuid(), (MinecartEntity) entity);
                } else {
                    AbstractMinecartEntity storedCart = map.get(playerEntity.getUuid());

                    if (storedCart == null || !storedCart.isAlive() || storedCart.isRemoved() || storedCart == entity) {
                        playerEntity.sendMessage(Text.literal("The first minecart is invalid. This is your first minecart, now."), false);
                        map.put(playerEntity.getUuid(), (MinecartEntity) entity);
                    } else {
                        BlockPos pos1 = storedCart.getBlockPos();
                        BlockPos pos2 = entity.getBlockPos();

                        if(pos1.getManhattanDistance(pos2) != 2) {
                            playerEntity.sendMessage(Text.literal("Minecarts must be exactly one block apart."), false);
                            map.remove(playerEntity.getUuid());
                            return ActionResult.SUCCESS;
                        } else {
                            playerEntity.sendMessage(Text.literal("You clicked the second minecart and linked it!"), false);
                            playerEntity.getMainHandStack().decrement(1);
                            map.remove(playerEntity.getUuid());
                        }
                    }
                }
            }
            return ActionResult.SUCCESS;
        });
    }
}
