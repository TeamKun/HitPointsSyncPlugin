package net.kunmc.lab.hitpointssyncplugin;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;

public final class HitPointsSyncPlugin extends JavaPlugin
{
    public static HashMap<String, HPManager> managers;

    @Override
    public void onEnable()
    {
        managers = new HashMap<>();

        managers.put("main", new HPManager(null));

        Bukkit.getOnlinePlayers().forEach(player -> {
            EventListener.onJoin(new PlayerJoinEvent(player, Component.text("")));
        });
    }
}
