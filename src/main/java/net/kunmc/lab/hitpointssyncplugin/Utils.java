package net.kunmc.lab.hitpointssyncplugin;

import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

public class Utils
{
    public static HPManager getManager(Player player)
    {
        Team team = player.getScoreboard().getEntryTeam(player.getName());
        if (team == null)
            return  HitPointsSyncPlugin.managers.get("main");
        else
            return  HitPointsSyncPlugin.managers.get(team.getName());
    }

    public static Integer parseInt(String str)
    {
        try
        {
            return Integer.parseInt(str);
        }
        catch (Exception ignored)
        {
            return null;
        }
    }

}
