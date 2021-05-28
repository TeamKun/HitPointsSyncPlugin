package net.kunmc.lab.hitpointssyncplugin.commands;

import net.kunmc.lab.hitpointssyncplugin.HPManager;
import net.kunmc.lab.hitpointssyncplugin.HitPointsSyncPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class RegisterCommand
{
    public static void register(CommandSender sender, String[] args)
    {
        if (args.length == 0)
        {
            sender.sendMessage(ChatColor.RED + "エラー：使用法が間違っています。使用法：/hpsync maxhp [チーム名] <最大HP(ハート数)>") ;
            return;
        }

        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();

        Team team =  scoreboard.getTeam(args[0]);

        if (team == null)
        {
            sender.sendMessage(ChatColor.RED + "エラー：チームが見つかりませんでした。");
            return;
        }

        if (HitPointsSyncPlugin.managers.get(team.getName()) == null)
        {
            sender.sendMessage(ChatColor.RED + "エラー：チームは既に登録されています。");
            return;
        }

        HitPointsSyncPlugin.managers.put(team.getName(), new HPManager(team));

        sender.sendMessage(ChatColor.GREEN + "チームを " + team.getName() + " として登録しました。");

    }
}
