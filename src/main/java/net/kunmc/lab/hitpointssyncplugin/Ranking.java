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
    public static Objective regen;

    static {
        init();
    }

    public static void init()
    {
        if ((damage = mainScoreboard.getObjective("damage")) == null)
            damage = mainScoreboard.registerNewObjective("damage", "dummy", Component.text("食らったダメージ量"));
        if ((regen = mainScoreboard.getObjective("regen")) == null)
            regen = mainScoreboard.registerNewObjective("regen", "dummy", Component.text("回復した量"));
    }

    public static void show(Mode mode)
    {
        switch (mode)
        {
            case DAMAGE:
                damage.setDisplaySlot(DisplaySlot.SIDEBAR);
                break;
            case REGEN:
                regen.setDisplaySlot(DisplaySlot.SIDEBAR);
        }
    }

    public static void clear()
    {
        damage.unregister();
        regen.unregister();
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
            case REGEN:
                score = regen.getScore(playerName);
                score.setScore(score.getScore() + amount);
        }
    }

    public enum Mode
    {
        DAMAGE,
        REGEN
    }
}
