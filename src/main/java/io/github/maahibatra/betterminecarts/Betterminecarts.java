package io.github.maahibatra.betterminecarts;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import java.util.HashMap;

public class Betterminecarts implements ModInitializer {

    @Override
    public void onInitialize() {
        System.out.println("BETTER MINECARTS LOADED SUCCESSFULLY.");

        UseEntityCallback.EVENT.register((playerEntity, world, hand, entity, entityHitResult) -> {
            if(!world.isClient() && entity.getType() == EntityType.MINECART && playerEntity.getMainHandStack().getItem() == Items.CHAIN) {
                playerEntity.sendMessage(Text.literal("You clicked a minecart with a chain!"), false);
            }
            return ActionResult.SUCCESS;
        });
    }
}
