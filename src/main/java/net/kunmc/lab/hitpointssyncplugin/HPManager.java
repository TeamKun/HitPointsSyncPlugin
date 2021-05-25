package net.kunmc.lab.hitpointssyncplugin;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class HPManager
{
    private static Scoreboard mainScoreboard;

    private final String name;

    private final Team team;

    private int maxHP;

    static
    {
        mainScoreboard = Bukkit.getServer().getScoreboardManager().getMainScoreboard();
    }

    public HPManager(Team team)
    {
        if (team == null)
        {
            this.name = "main";
            if (mainScoreboard.getTeam(name) == null)
                this.team = mainScoreboard.registerNewTeam(name);
            else
                this.team = mainScoreboard.getTeam(name);
        }
        else
        {
            this.name = team.getName();
            this.team = team;
        }

        this.maxHP = 20;
    }

    public int getMaxHP()
    {
        return maxHP;
    }

    public void setMaxHP(int maxHP)
    {
        team.getEntries()
                .forEach(s -> {
                    Player player = Bukkit.getPlayer(s);
                    if (player != null)
                        player.getAttribute(Attribute.GENERIC_MAX_HEALTH)
                                .setBaseValue(maxHP);
                });
        this.maxHP = maxHP;
    }
}
