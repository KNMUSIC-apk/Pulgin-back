package com.backondie.command;

import com.backondie.BackOnDie;
import com.backondie.config.ConfigManager;
import com.backondie.database.DeathRecord;
import com.backondie.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class BackCommand implements CommandExecutor, TabCompleter, Listener {

    private final BackOnDie plugin;
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private final Map<UUID, BukkitTask> activeWarmups = new HashMap<>();

    public BackCommand(BackOnDie plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        ConfigManager cfg = plugin.getConfigManager();

        // Lệnh Quản Trị
        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("reload")) {
                if (!sender.hasPermission("backondie.admin")) {
                    sender.sendMessage(MessageUtil.parse(cfg.getMessage("no-permission")));
                    return true;
                }
                cfg.loadConfig();
                sender.sendMessage(MessageUtil.parse(cfg.getMessage("reload-success")));
                return true;
            }

            if (args[0].equalsIgnoreCase("clear") && args.length > 1) {
                if (!sender.hasPermission("backondie.admin")) {
                    sender.sendMessage(MessageUtil.parse(cfg.getMessage("no-permission")));
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                UUID targetUuid = target != null ? target.getUniqueId() : Bukkit.getOfflinePlayer(args[1]).getUniqueId();
                plugin.getDatabaseManager().clearDeaths(targetUuid).thenRun(() -> {
                    sender.sendMessage(MessageUtil.parse(cfg.getMessage("clear-success").replace("<player>", args[1])));
                });
                return true;
            }
        }

        // Lệnh /back chính cho Người chơi
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Lệnh này chỉ dành cho người chơi!");
            return true;
        }

        if (!player.hasPermission("backondie.use")) {
            MessageUtil.sendMessage(player, cfg.getMessage("no-permission"));
            return true;
        }

        // Kiểm tra Cooldown
        long now = System.currentTimeMillis();
        long cooldownTime = cfg.getCooldownSeconds() * 1000L;
        if (cooldowns.containsKey(player.getUniqueId())) {
            long timeLeft = (cooldowns.get(player.getUniqueId()) + cooldownTime) - now;
            if (timeLeft > 0) {
                MessageUtil.sendMessage(player, cfg.getMessage("cooldown").replace("<time>", String.valueOf(timeLeft / 1000)));
                MessageUtil.playSound(player, cfg.getSound("fail-sound"));
                return true;
            }
        }

        // Truy vấn dữ liệu chết Asynchronous
        plugin.getDatabaseManager().getLatestDeath(player.getUniqueId()).thenAccept(record -> {
            // Chuyển thao tác kiểm tra & teleport về Main Server Thread
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (record == null) {
                    MessageUtil.sendMessage(player, cfg.getMessage("no-death-record"));
                    MessageUtil.playSound(player, cfg.getSound("fail-sound"));
                    return;
                }

                Location deathLoc = record.toLocation();
                if (deathLoc == null) {
                    MessageUtil.sendMessage(player, cfg.getMessage("world-not-found"));
                    MessageUtil.playSound(player, cfg.getSound("fail-sound"));
                    return;
                }

                // Kiểm tra sự tồn tại của mộ
                if (!plugin.getGraveManager().isGraveValid(player, record.graveId(), deathLoc)) {
                    MessageUtil.sendMessage(player, cfg.getMessage("grave-not-found"));
                    MessageUtil.playSound(player, cfg.getSound("fail-sound"));
                    return;
                }

                // Tiến hành Warmup nếu được cấu hình
                int warmupSeconds = cfg.getWarmupSeconds();
                if (warmupSeconds > 0) {
                    startWarmup(player, deathLoc, warmupSeconds);
                } else {
                    executeTeleport(player, deathLoc);
                }
            });
        });

        return true;
    }

    private void startWarmup(Player player, Location targetLoc, int seconds) {
        ConfigManager cfg = plugin.getConfigManager();
        MessageUtil.sendMessage(player, cfg.getMessage("warmup-start").replace("<time>", String.valueOf(seconds)));

        BukkitTask task = new BukkitRunnable() {
            int time = seconds;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancelWarmup(player.getUniqueId());
                    return;
                }

                if (time <= 0) {
                    cancelWarmup(player.getUniqueId());
                    executeTeleport(player, targetLoc);
                    return;
                }

                MessageUtil.sendActionBar(player, cfg.getMessage("actionbar.warmup").replace("<time>", String.valueOf(time)));
                MessageUtil.playSound(player, cfg.getSound("warmup-sound"));
                time--;
            }
        }.runTaskTimer(plugin, 0L, 20L);

        activeWarmups.put(player.getUniqueId(), task);
    }

    private void executeTeleport(Player player, Location loc) {
        ConfigManager cfg = plugin.getConfigManager();

        player.teleportAsync(loc).thenAccept(success -> {
            if (success) {
                cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
                MessageUtil.sendMessage(player, cfg.getMessage("teleport-success"));
                MessageUtil.playSound(player, cfg.getSound("teleport-sound"));
                MessageUtil.spawnParticles(loc, cfg.getParticle(), cfg.getParticleCount());

                if (cfg.getConfig().getBoolean("messages.title.enabled")) {
                    MessageUtil.sendTitle(
                            player,
                            cfg.getMessage("title.main"),
                            cfg.getMessage("title.sub"),
                            cfg.getConfig().getInt("messages.title.fade-in"),
                            cfg.getConfig().getInt("messages.title.stay"),
                            cfg.getConfig().getInt("messages.title.fade-out")
                    );
                }
            }
        });
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!plugin.getConfigManager().isCancelOnMove()) return;

        if (activeWarmups.containsKey(player.getUniqueId())) {
            if (event.getFrom().getBlockX() != event.getTo().getBlockX() ||
                event.getFrom().getBlockY() != event.getTo().getBlockY() ||
                event.getFrom().getBlockZ() != event.getTo().getBlockZ()) {

                cancelWarmup(player.getUniqueId());
                MessageUtil.sendMessage(player, plugin.getConfigManager().getMessage("warmup-cancelled"));
                MessageUtil.playSound(player, plugin.getConfigManager().getSound("fail-sound"));
            }
        }
    }

    private void cancelWarmup(UUID uuid) {
        BukkitTask task = activeWarmups.remove(uuid);
        if (task != null) {
            task.cancel();
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1 && sender.hasPermission("backondie.admin")) {
            return List.of("reload", "clear", "info");
        }
        return Collections.emptyList();
    }
}
