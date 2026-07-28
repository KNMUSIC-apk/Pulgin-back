package com.backondie.config;

import com.backondie.BackOnDie;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {

    private final BackOnDie plugin;
    private FileConfiguration config;

    public ConfigManager(BackOnDie plugin) {
        this.plugin = plugin;
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }

    public String getDatabaseType() { return config.getString("database.type", "SQLITE"); }
    public String getMysqlHost() { return config.getString("database.mysql.host", "localhost"); }
    public int getMysqlPort() { return config.getInt("database.mysql.port", 3306); }
    public String getMysqlDatabase() { return config.getString("database.mysql.database", "minecraft"); }
    public String getMysqlUsername() { return config.getString("database.mysql.username", "root"); }
    public String getMysqlPassword() { return config.getString("database.mysql.password", ""); }
    public int getMysqlPoolSize() { return config.getInt("database.mysql.pool-size", 10); }

    public boolean isRequireExistingGrave() { return config.getBoolean("settings.require-existing-grave", true); }
    public int getWarmupSeconds() { return config.getInt("settings.warmup-seconds", 5); }
    public boolean isCancelOnMove() { return config.getBoolean("settings.cancel-on-move", true); }
    public int getCooldownSeconds() { return config.getInt("settings.cooldown-seconds", 60); }

    public String getPrefix() { return config.getString("messages.prefix", ""); }
    public String getMessage(String path) { return config.getString("messages." + path, ""); }
    public String getSound(String path) { return config.getString("effects." + path, ""); }
    public String getParticle() { return config.getString("effects.particle", "PORTAL"); }
    public int getParticleCount() { return config.getInt("effects.particle-count", 50); }
}
