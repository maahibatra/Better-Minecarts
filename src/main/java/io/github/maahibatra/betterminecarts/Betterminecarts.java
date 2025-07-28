package io.github.maahibatra.betterminecarts;

import io.github.maahibatra.betterminecarts.items.ChainItem;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;

public class Betterminecarts implements ModInitializer {

    // Register the chain item
    public static final Item CHAIN_ITEM = new ChainItem(new FabricItemSettings());

    @Override
    public void onInitialize() {
        // Register our custom chain item
        Registry.register(Registries.ITEM, new Identifier("betterminecarts", "chain"), CHAIN_ITEM);
        
        // This is a simple exploration hook - when you right-click on a minecart,
        // it will tell you information about that minecart
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (entity instanceof AbstractMinecartEntity minecart) {
                if (!world.isClient && player instanceof ServerPlayerEntity serverPlayer) {
                    // Send some debugging info to the player
                    player.sendMessage(Text.literal("Minecart Debug Info:"), false);
                    player.sendMessage(Text.literal("- Position: " + minecart.getPos()), false);
                    player.sendMessage(Text.literal("- Velocity: " + minecart.getVelocity()), false);
                    player.sendMessage(Text.literal("- UUID: " + minecart.getUuid()), false);
                    player.sendMessage(Text.literal("- Type: " + minecart.getType().toString()), false);
                    
                    // This shows you there are passengers (entities riding the cart)
                    if (!minecart.getPassengerList().isEmpty()) {
                        player.sendMessage(Text.literal("- Has passengers: " + minecart.getPassengerList().size()), false);
                    }
                    
                    // Use our detailed explorer
                    MinecartExplorer.exploreMinecart(minecart, serverPlayer);
                }
                return ActionResult.SUCCESS;
            }
            return ActionResult.PASS;
        });
        
        System.out.println("BetterMinecarts mod initialized!");
        System.out.println("- Right-click minecarts to see debug info");
        System.out.println("- Use /give @s betterminecarts:chain to get a chain item");
        System.out.println("- Use the chain item to explore minecart interactions");
    }
}
