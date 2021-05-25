package net.kunmc.lab.hitpointssyncplugin;

import org.bukkit.attribute.Attribute;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class EventListener implements Listener
{
    @EventHandler
    public static void onJoin(PlayerJoinEvent e)
    {
        e.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH)
                .setBaseValue(Utils.getManager(e.getPlayer()).getMaxHP());
    }

    @EventHandler
    public static void onLeave(PlayerQuitEvent e)
    {
        e.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH)
                .setBaseValue(20);
    }
}
