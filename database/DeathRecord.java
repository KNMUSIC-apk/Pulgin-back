package com.backondie.database;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.UUID;

public record DeathRecord(
        int id,
        UUID playerUuid,
        String worldName,
        double x,
        double y,
        double z,
        float yaw,
        float pitch,
        long timestamp,
        String itemsBase64,
        String graveId
) {
    public Location toLocation() {
        World world = Bukkit.getWorld(worldName);
        if (world == null) return null;
        return new Location(world, x, y, z, yaw, pitch);
    }
}
