package io.github.maahibatra.betterminecarts;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

public class Betterminecarts implements ModInitializer {

    @Override
    public void onInitialize() {
        System.out.println("BETTER MINECARTS LOADED SUCCESSFULLY.");

        UseEntityCallback.EVENT.register((playerEntity, world, hand, entity, entityHitResult) -> {
            if(!world.isClient()) {
                playerEntity.sendMessage(Text.literal("Entity clicked: " + entity.getType()), false);
            }
            return ActionResult.SUCCESS;
        });
    }
}
