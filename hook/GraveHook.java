package com.backondie.hook;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface GraveHook {
    /**
     * Tên của Provider (Graves / GraveStone, v.v.)
     */
    String getName();

    /**
     * Kiểm tra xem ngôi mộ với ID hoặc vị trí cho trước còn tồn tại hay không
     */
    boolean isGraveExisting(Player player, String graveId, Location deathLocation);
}
