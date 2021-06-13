package net.kunmc.lab.hitpointssyncplugin;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

public class Ranking
{
    public static Scoreboard mainScoreboard = Bukkit.getScoreboardManager().getMainScoreboard();

    public static Objective damage;

    static {
        init();
    }

    public static void init()
    {
        if ((damage = mainScoreboard.getObjective("damage")) == null)
            damage = mainScoreboard.registerNewObjective("damage", "dummy", Component.text("食らったダメージ量"));
 }

    public static void show(Mode mode)
    {
        if (mode == Mode.DAMAGE)
            damage.setDisplaySlot(DisplaySlot.SIDEBAR);
    }

    public static void clear()
    {
        if (damage != null)
        damage.unregister();
        init();
    }


    public static void push(Mode mode, String playerName, int amount)
    {
        switch (mode)
        {
            case DAMAGE:
                Score score = damage.getScore(playerName);
                score.setScore(score.getScore() + amount);
                break;
        }
    }

    public enum Mode
    {
        DAMAGE
    }
}
