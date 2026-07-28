package com.backondie;

import com.backondie.command.BackCommand;
import com.backondie.config.ConfigManager;
import com.backondie.database.DatabaseManager;
import com.backondie.hook.GraveManager;
import com.backondie.hook.PlaceholderAPIHook;
import com.backondie.listener.DeathListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class BackOnDie extends JavaPlugin {

    private static BackOnDie instance;
    private ConfigManager configManager;
    private DatabaseManager databaseManager;
    private GraveManager graveManager;

    @Override
    public void onEnable() {
        instance = this;

        // 1. Khởi tạo Cấu hình
        this.configManager = new ConfigManager(this);
        this.configManager.loadConfig();

        // 2. Khởi tạo Cơ sở dữ liệu
        this.databaseManager = new DatabaseManager(this);
        this.databaseManager.init();

        // 3. Khởi tạo Grave Hook Manager
        this.graveManager = new GraveManager(this);
        this.graveManager.registerHooks();

        // 4. Khởi tạo PlaceholderAPI Hook
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new PlaceholderAPIHook(this).register();
        }

        // 5. Đăng ký Listener & Command
        getServer().getPluginManager().registerEvents(new DeathListener(this), this);
        if (getCommand("back") != null) {
            BackCommand backCmd = new BackCommand(this);
            getCommand("back").setExecutor(backCmd);
            getCommand("back").setTabCompleter(backCmd);
        }

        getLogger().info("BackOnDie v" + getPluginMeta().getVersion() + " đã kích hoạt thành công!");
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.close();
        }
        getLogger().info("BackOnDie đã tắt!");
    }

    public static BackOnDie getInstance() { return instance; }
    public ConfigManager getConfigManager() { return configManager; }
    public DatabaseManager getDatabaseManager() { return databaseManager; }
    public GraveManager getGraveManager() { return graveManager; }
}
