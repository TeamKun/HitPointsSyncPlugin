package net.kunmc.lab.hitpointssyncplugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class EventListener implements Listener
{
    @EventHandler
    public static void onJoin(PlayerJoinEvent e)
    {

        HPManager manager = Utils.getManager(e.getPlayer());

        if (manager == null)
        {
            Bukkit.getScoreboardManager().getMainScoreboard().getTeam("main").addEntry(e.getPlayer().getName());
            manager = Utils.getManager(e.getPlayer());
        }


        if (manager == null || !manager.isStarted())
            return;
        e.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH)
                .setBaseValue(manager.getMaxHP());
    }

    @EventHandler
    public static void onLeave(PlayerQuitEvent e)
    {
        e.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH)
                .setBaseValue(20);
    }

    @EventHandler
    public static void onDamage(EntityDamageEvent e)
    {
        if (!(e.getEntity() instanceof Player))
            return;

        if (e.getFinalDamage() < 1.0d)
            return;


        if (e.getCause() == EntityDamageEvent.DamageCause.SUICIDE)
            return;

        HPManager manager = Utils.getManager((Player) e.getEntity());

        if (manager == null || !manager.isStarted())
            return;

        e.setCancelled(true);
        manager.applyDamage((Player) e.getEntity(), e.getCause(), e.getFinalDamage());
    }

    @EventHandler
    public static void onRegen(EntityRegainHealthEvent e)
    {
        if (!(e.getEntity() instanceof Player))
            return;

        HPManager manager = Utils.getManager((Player) e.getEntity());

        if (manager == null || !manager.isStarted())
            return;

        boolean regenSuccess = manager.regen(e.getAmount());

        if (!regenSuccess && e.getRegainReason() == EntityRegainHealthEvent.RegainReason.EATING)
        {
            e.getEntity().sendMessage(ChatColor.RED + "回復はクールダウン中です。");
            e.setCancelled(true);
            return;
        }

        if (!regenSuccess)
            e.setCancelled(true);
        e.setAmount(0);
    }

}
