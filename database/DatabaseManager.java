package com.backondie.database;

import com.backondie.BackOnDie;
import com.backondie.config.ConfigManager;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.File;
import java.sql.*;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class DatabaseManager {

    private final BackOnDie plugin;
    private HikariDataSource dataSource;

    public DatabaseManager(BackOnDie plugin) {
        this.plugin = plugin;
    }

    public void init() {
        ConfigManager cfg = plugin.getConfigManager();
        HikariConfig hikari = new HikariConfig();

        if (cfg.getDatabaseType().equalsIgnoreCase("MYSQL")) {
            hikari.setJdbcUrl("jdbc:mysql://" + cfg.getMysqlHost() + ":" + cfg.getMysqlPort() + "/" + cfg.getMysqlDatabase() + "?useSSL=false&allowPublicKeyRetrieval=true");
            hikari.setUsername(cfg.getMysqlUsername());
            hikari.setPassword(cfg.getMysqlPassword());
            hikari.setMaximumPoolSize(cfg.getMysqlPoolSize());
        } else {
            File dbFile = new File(plugin.getDataFolder(), "data.db");
            hikari.setJdbcUrl("jdbc:sqlite:" + dbFile.getAbsolutePath());
            hikari.setMaximumPoolSize(5);
        }

        this.dataSource = new HikariDataSource(hikari);
        createTables();
    }

    private void createTables() {
        CompletableFuture.runAsync(() -> {
            String query = """
                CREATE TABLE IF NOT EXISTS backondie_deaths (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    player_uuid VARCHAR(36) NOT NULL,
                    world VARCHAR(64) NOT NULL,
                    x DOUBLE NOT NULL,
                    y DOUBLE NOT NULL,
                    z DOUBLE NOT NULL,
                    yaw FLOAT NOT NULL,
                    pitch FLOAT NOT NULL,
                    timestamp BIGINT NOT NULL,
                    items TEXT,
                    grave_id VARCHAR(128)
                );
            """;
            if (plugin.getConfigManager().getDatabaseType().equalsIgnoreCase("MYSQL")) {
                query = query.replace("AUTOINCREMENT", "AUTO_INCREMENT");
            }
            try (Connection conn = dataSource.getConnection(); Statement stmt = conn.createStatement()) {
                stmt.execute(query);
            } catch (SQLException e) {
                plugin.getLogger().severe("Lỗi khi khởi tạo Bảng Database: " + e.getMessage());
            }
        });
    }

    public CompletableFuture<Void> saveDeath(UUID uuid, String world, double x, double y, double z, float yaw, float pitch, String items, String graveId) {
        return CompletableFuture.runAsync(() -> {
            String sql = "INSERT INTO backondie_deaths (player_uuid, world, x, y, z, yaw, pitch, timestamp, items, grave_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, uuid.toString());
                ps.setString(2, world);
                ps.setDouble(3, x);
                ps.setDouble(4, y);
                ps.setDouble(5, z);
                ps.setFloat(6, yaw);
                ps.setFloat(7, pitch);
                ps.setLong(8, System.currentTimeMillis());
                ps.setString(9, items);
                ps.setString(10, graveId);
                ps.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().severe("Lỗi khi lưu bản ghi chết bất đồng bộ: " + e.getMessage());
            }
        });
    }

    public CompletableFuture<DeathRecord> getLatestDeath(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT * FROM backondie_deaths WHERE player_uuid = ? ORDER BY id DESC LIMIT 1";
            try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, uuid.toString());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    return new DeathRecord(
                            rs.getInt("id"),
                            UUID.fromString(rs.getString("player_uuid")),
                            rs.getString("world"),
                            rs.getDouble("x"),
                            rs.getDouble("y"),
                            rs.getDouble("z"),
                            rs.getFloat("yaw"),
                            rs.getFloat("pitch"),
                            rs.getLong("timestamp"),
                            rs.getString("items"),
                            rs.getString("grave_id")
                    );
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Lỗi khi lấy dữ liệu chết: " + e.getMessage());
            }
            return null;
        });
    }

    public CompletableFuture<Void> clearDeaths(UUID uuid) {
        return CompletableFuture.runAsync(() -> {
            String sql = "DELETE FROM backondie_deaths WHERE player_uuid = ?";
            try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, uuid.toString());
                ps.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().severe("Lỗi khi xóa dữ liệu chết: " + e.getMessage());
            }
        });
    }

    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}
