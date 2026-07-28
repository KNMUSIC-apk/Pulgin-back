package com.backondie.util;

import com.backondie.BackOnDie;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.time.Duration;

public class MessageUtil {

    private static final MiniMessage mm = MiniMessage.miniMessage();

    public static Component parse(String message) {
        String prefix = BackOnDie.getInstance().getConfigManager().getPrefix();
        return mm.deserialize(prefix + message);
    }

    public static Component parseNoPrefix(String message) {
        return mm.deserialize(message);
    }

    public static void sendMessage(Player player, String message) {
        player.sendMessage(parse(message));
    }

    public static void sendActionBar(Player player, String message) {
        player.sendActionBar(parseNoPrefix(message));
    }

    public static void sendTitle(Player player, String mainTitle, String subTitle, int fadeIn, int stay, int fadeOut) {
        Title title = Title.title(
                parseNoPrefix(mainTitle),
                parseNoPrefix(subTitle),
                Title.Times.times(Duration.ofMillis(fadeIn * 50L), Duration.ofMillis(stay * 50L), Duration.ofMillis(fadeOut * 50L))
        );
        player.showTitle(title);
    }

    public static void playSound(Player player, String soundName) {
        if (soundName == null || soundName.isEmpty()) return;
        try {
            Sound sound = Sound.valueOf(soundName.toUpperCase());
            player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
        } catch (IllegalArgumentException ignored) {}
    }

    public static void spawnParticles(Location loc, String particleName, int count) {
        if (particleName == null || particleName.isEmpty()) return;
        try {
            Particle particle = Particle.valueOf(particleName.toUpperCase());
            loc.getWorld().spawnParticle(particle, loc, count, 0.5, 1.0, 0.5, 0.1);
        } catch (IllegalArgumentException ignored) {}
    }
}
