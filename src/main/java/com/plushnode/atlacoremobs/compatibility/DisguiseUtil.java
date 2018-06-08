package com.plushnode.atlacoremobs.compatibility;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;

public final class DisguiseUtil {
    private static boolean enabled = false;

    static {
        enabled = Bukkit.getPluginManager().getPlugin("LibsDisguises") != null;
    }

    private DisguiseUtil() {

    }

    public static void disguise(Entity entity, String name, String skinName) {
        if (!enabled) return;

        PlayerDisguise disguise = new PlayerDisguise(ChatColor.translateAlternateColorCodes('&', name), skinName);
        entity.setCustomName(ChatColor.translateAlternateColorCodes('&', name));
        DisguiseAPI.disguiseEntity(entity, disguise);
    }
}
