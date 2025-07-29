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

import javax.swing.*;
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
                    playerEntity.getMainHandStack().decrement(1);
                    map.put(playerEntity.getUuid(), (MinecartEntity) entity);
                } else {
                    playerEntity.sendMessage(Text.literal("You clicked the second minecart and linked it!"), false);
                    map.remove(playerEntity.getUuid());
                }
            }
            return ActionResult.SUCCESS;
        });
    }
}
