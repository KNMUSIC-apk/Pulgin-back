package com.backondie.hook;

import com.backondie.BackOnDie;
import com.backondie.database.DeathRecord;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class PlaceholderAPIHook extends PlaceholderExpansion {

    private final BackOnDie plugin;

    public PlaceholderAPIHook(BackOnDie plugin) {
        this.plugin = plugin;
    }

    @Override public @NotNull String getIdentifier() { return "backondie"; }
    @Override public @NotNull String getAuthor() { return "BackOnDieTeam"; }
    @Override public @NotNull String getVersion() { return plugin.getPluginMeta().getVersion(); }
    @Override public boolean persist() { return true; }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) return "";

        if (params.equalsIgnoreCase("last_death_world")) {
            DeathRecord record = plugin.getDatabaseManager().getLatestDeath(player.getUniqueId()).join();
            return record != null ? record.worldName() : "None";
        }
        if (params.equalsIgnoreCase("last_death_x")) {
            DeathRecord record = plugin.getDatabaseManager().getLatestDeath(player.getUniqueId()).join();
            return record != null ? String.valueOf((int) record.x()) : "0";
        }
        if (params.equalsIgnoreCase("last_death_y")) {
            DeathRecord record = plugin.getDatabaseManager().getLatestDeath(player.getUniqueId()).join();
            return record != null ? String.valueOf((int) record.y()) : "0";
        }
        if (params.equalsIgnoreCase("last_death_z")) {
            DeathRecord record = plugin.getDatabaseManager().getLatestDeath(player.getUniqueId()).join();
            return record != null ? String.valueOf((int) record.z()) : "0";
        }

        return null;
    }
}
