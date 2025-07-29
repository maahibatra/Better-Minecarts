package io.github.maahibatra.betterminecarts;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.MinecartEntity;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;

import java.util.HashMap;
import java.util.UUID;

public class Betterminecarts implements ModInitializer {

    @Override
    public void onInitialize() {
        System.out.println("BETTER MINECARTS LOADED SUCCESSFULLY.");

        HashMap<UUID, AbstractMinecartEntity> map = new HashMap<>();

        UseEntityCallback.EVENT.register((playerEntity, world, hand, entity, entityHitResult) -> {
            if(!world.isClient() && entity.getType() == EntityType.MINECART && playerEntity.getMainHandStack().getItem() == Items.CHAIN) {

                if(map.size() == 0) {
                    playerEntity.sendMessage(Text.literal("You clicked the first minecart with a chain!"), false);
                    map.put(playerEntity.getUuid(), (MinecartEntity) entity);
                } else {
                    AbstractMinecartEntity storedCart = map.get(playerEntity.getUuid());

                    if (storedCart == null || !storedCart.isAlive() || storedCart.isRemoved()) {
                        playerEntity.sendMessage(Text.literal("The first minecart is no longer valid. This is your first minecart, now."), false);
                        map.put(playerEntity.getUuid(), (MinecartEntity) entity);
                    } else {
                        playerEntity.sendMessage(Text.literal("You clicked the second minecart and linked it!"), false);
                        playerEntity.getMainHandStack().decrement(1);
                        map.remove(playerEntity.getUuid());
                    }
                }
            }
            return ActionResult.SUCCESS;
        });
    }
}
