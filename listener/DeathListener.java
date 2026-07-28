package com.backondie.listener;

import com.backondie.BackOnDie;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.UUID;

public class DeathListener implements Listener {

    private final BackOnDie plugin;

    public DeathListener(BackOnDie plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        UUID uuid = player.getUniqueId();
        var loc = player.getLocation();

        // Mã hóa vật phẩm sang Base64
        String itemsBase64 = serializeItems(player.getInventory().getContents());
        
        // Tạo Grave ID độc lập cho từng lần chết để tránh ghi đè
        String uniqueGraveId = "grave-" + uuid + "-" + System.currentTimeMillis();

        // Lưu hoàn toàn bất đồng bộ không làm lag main thread
        plugin.getDatabaseManager().saveDeath(
                uuid,
                loc.getWorld().getName(),
                loc.getX(),
                loc.getY(),
                loc.getZ(),
                loc.getYaw(),
                loc.getPitch(),
                itemsBase64,
                uniqueGraveId
        );
    }

    private String serializeItems(ItemStack[] items) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream)) {
            dataOutput.writeInt(items.length);
            for (ItemStack item : items) {
                dataOutput.writeObject(item);
            }
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (Exception e) {
            return "";
        }
    }
}
