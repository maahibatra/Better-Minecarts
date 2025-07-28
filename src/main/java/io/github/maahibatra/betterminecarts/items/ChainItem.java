package io.github.maahibatra.betterminecarts.items;

import io.github.maahibatra.betterminecarts.MinecartExplorer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

/**
 * A simple chain item for experimenting with linking minecarts
 * 
 * This shows you:
 * 1. How to create custom items that interact with entities
 * 2. How to store data in item NBT (which minecart you clicked first)
 * 3. How to find and interact with minecarts in the world
 */
public class ChainItem extends Item {
    
    public ChainItem(Settings settings) {
        super(settings);
    }
    
    @Override
    public TypedActionResult<ItemUsageContext> useOnBlock(ItemUsageContext context) {
        // When you right-click with the chain item
        World world = context.getWorld();
        PlayerEntity player = context.getPlayer();
        
        if (!world.isClient && player instanceof ServerPlayerEntity serverPlayer) {
            // Look for nearby minecarts
            world.getEntitiesByClass(AbstractMinecartEntity.class, 
                player.getBoundingBox().expand(5.0), // 5 block radius
                minecart -> true)
                .forEach(minecart -> {
                    serverPlayer.sendMessage(Text.literal("Found minecart at: " + minecart.getPos()), false);
                    
                    // Use our explorer to see what's in this minecart
                    MinecartExplorer.exploreMinecart(minecart, serverPlayer);
                });
                
            serverPlayer.sendMessage(Text.literal("Chain used! Look for nearby minecarts."), false);
        }
        
        return TypedActionResult.success(context.getStack());
    }
    
    /**
     * This is where you could implement the actual chain linking logic:
     * 
     * 1. First click: Store the minecart UUID in the item's NBT
     * 2. Second click: Link the stored minecart with the new one
     * 
     * The key concepts you'd need:
     * - Item NBT for temporary storage
     * - Entity NBT for permanent linkage data
     * - Distance checking
     * - Visual feedback for the player
     */
    private void linkMinecarts(AbstractMinecartEntity first, AbstractMinecartEntity second, PlayerEntity player) {
        // This is where your actual linking logic would go
        // For now, just a placeholder that shows the concept
        
        if (player instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.sendMessage(Text.literal("Would link " + first.getUuid() + " to " + second.getUuid()), false);
            
            // You could call MinecartExplorer.linkMinecarts(first, second) here
            // Then you'd need to implement the physics in a mixin or event handler
        }
    }
}