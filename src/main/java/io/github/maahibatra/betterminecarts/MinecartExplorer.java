package io.github.maahibatra.betterminecarts;

import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * This class helps you explore and understand minecart entities
 * 
 * Key things to understand about minecarts:
 * 1. They extend AbstractMinecartEntity
 * 2. They have position, velocity, and rotation
 * 3. They can store NBT data (persistent data)
 * 4. They update every tick (20 times per second)
 * 5. They have different types (normal, chest, furnace, etc.)
 */
public class MinecartExplorer {
    
    /**
     * This method shows you what data is available in a minecart
     */
    public static void exploreMinecart(AbstractMinecartEntity minecart, ServerPlayerEntity player) {
        player.sendMessage(Text.literal("=== MINECART DEEP DIVE ==="), false);
        
        // Position and movement data
        player.sendMessage(Text.literal("Position: " + minecart.getX() + ", " + minecart.getY() + ", " + minecart.getZ()), false);
        player.sendMessage(Text.literal("Velocity: " + minecart.getVelocity()), false);
        player.sendMessage(Text.literal("On Ground: " + minecart.isOnGround()), false);
        
        // Entity relationships
        player.sendMessage(Text.literal("Passengers: " + minecart.getPassengerList().size()), false);
        if (minecart.hasVehicle()) {
            player.sendMessage(Text.literal("Is riding: " + minecart.getVehicle().getType()), false);
        }
        
        // NBT data - this is where you could store chain linkage info
        NbtCompound nbt = new NbtCompound();
        minecart.writeNbt(nbt);
        player.sendMessage(Text.literal("NBT keys: " + nbt.getKeys()), false);
        
        // This is important - entities have unique IDs that persist across saves
        player.sendMessage(Text.literal("UUID: " + minecart.getUuid()), false);
        
        // Age and other properties
        player.sendMessage(Text.literal("Age: " + minecart.age), false);
        player.sendMessage(Text.literal("Entity ID: " + minecart.getId()), false);
        
        player.sendMessage(Text.literal("========================="), false);
    }
    
    /**
     * For your chain linking feature, you might want to:
     * 1. Store linked minecart UUIDs in NBT data
     * 2. Check distance between linked carts
     * 3. Apply forces/movement to linked carts
     * 4. Handle chain breaking when too far apart
     * 
     * Here's a simple example of how you might store a linked cart:
     */
    public static void linkMinecarts(AbstractMinecartEntity cart1, AbstractMinecartEntity cart2) {
        // Store each cart's UUID in the other's NBT data
        NbtCompound cart1Data = new NbtCompound();
        cart1.writeNbt(cart1Data);
        cart1Data.putUuid("linked_cart", cart2.getUuid());
        cart1.readNbt(cart1Data);
        
        NbtCompound cart2Data = new NbtCompound();
        cart2.writeNbt(cart2Data);
        cart2Data.putUuid("linked_cart", cart1.getUuid());
        cart2.readNbt(cart2Data);
    }
    
    /**
     * Check if two carts are linked
     */
    public static boolean areLinked(AbstractMinecartEntity cart1, AbstractMinecartEntity cart2) {
        NbtCompound nbt = new NbtCompound();
        cart1.writeNbt(nbt);
        
        if (nbt.containsUuid("linked_cart")) {
            return nbt.getUuid("linked_cart").equals(cart2.getUuid());
        }
        
        return false;
    }
}