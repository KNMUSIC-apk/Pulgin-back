package com.backondie.hook;

import com.backondie.BackOnDie;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class GraveManager {

    private final BackOnDie plugin;
    private final List<GraveHook> hooks = new ArrayList<>();

    public GraveManager(BackOnDie plugin) {
        this.plugin = plugin;
    }

    public void registerHooks() {
        Plugin gravesPlugin = Bukkit.getPluginManager().getPlugin("Graves");
        if (gravesPlugin != null && gravesPlugin.isEnabled()) {
            hooks.add(new CustomGravesHook());
            plugin.getLogger().info("Đã tích hợp thành công với plugin Graves!");
        }

        // Đăng ký bổ sung các provider mộ khác ở đây
    }

    public void addCustomHook(GraveHook hook) {
        hooks.add(hook);
    }

    public boolean isGraveValid(Player player, String graveId, Location loc) {
        if (!plugin.getConfigManager().isRequireExistingGrave()) {
            return true; // Nếu cấu hình cho phép bỏ qua kiểm tra mộ
        }
        if (hooks.isEmpty()) {
            return true; // Nếu không có plugin mộ nào được kích hoạt
        }

        for (GraveHook hook : hooks) {
            if (hook.isGraveExisting(player, graveId, loc)) {
                return true;
            }
        }
        return false;
    }

    // Class triển khai ví dụ tích hợp API Graves
    private static class CustomGravesHook implements GraveHook {
        @Override
        public String getName() { return "Graves"; }

        @Override
        public boolean isGraveExisting(Player player, String graveId, Location deathLocation) {
            // Thực hiện kiểm tra API từ plugin Graves/GraveStone tại vị trí chết
            // Giả lập logic kiểm tra khối/thực thể mộ còn tồn tại tại vị trí
            return deathLocation.getBlock().getType().name().contains("GRAVE") 
                   || deathLocation.getChunk().isLoaded(); // Thay bằng API trực tiếp của Graves
        }
    }
}
