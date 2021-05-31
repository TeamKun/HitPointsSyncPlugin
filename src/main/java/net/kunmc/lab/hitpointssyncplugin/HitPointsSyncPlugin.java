package net.kunmc.lab.hitpointssyncplugin;

import net.kunmc.lab.hitpointssyncplugin.commands.MainCommand;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public final class HitPointsSyncPlugin extends JavaPlugin
{
    public static HashMap<String, HPManager> managers;
    public static List<String> activeManagers;

    public static boolean started;

    public static HitPointsSyncPlugin instance;

    @Override
    public void onEnable()
    {
        instance = this;
        getCommand("hpsync").setExecutor(new MainCommand());
        getCommand("hpsync").setTabCompleter(new MainCommand());
        Bukkit.getPluginManager().registerEvents(new EventListener(), this);

        managers = new HashMap<>();
        activeManagers = new ArrayList<>();

        managers.put("main", new HPManager(null));

        Bukkit.getOnlinePlayers().forEach(player -> {
            EventListener.onJoin(new PlayerJoinEvent(player, Component.text("")));
        });
    }
}
