package net.kunmc.lab.hitpointssyncplugin.commands;

import net.kunmc.lab.hitpointssyncplugin.Ranking;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.scoreboard.DisplaySlot;

public class RankingCommand
{
    public static void ranking(CommandSender sender, String[] args)
    {
        if (args.length < 1)
        {
            sender.sendMessage(ChatColor.RED + "エラー：引数が足りません");
            return;
        }

        String mode = args[0];

        switch (mode)
        {
            case "damage":
                Ranking.show(Ranking.Mode.DAMAGE);
                break;
            case "none":
                Bukkit.getScoreboardManager().getMainScoreboard().clearSlot(DisplaySlot.SIDEBAR);
            case "clear":
                Ranking.clear();
                sender.sendMessage(ChatColor.GREEN + "ランキングをクリアしました。");
                break;
            default:
                sender.sendMessage(ChatColor.RED + "不明なモードです。モード一覧：damage(ダメージ), none(表示しない), clear(ランキングをクリア)");
                return;
        }
        sender.sendMessage(ChatColor.GREEN + "サイドバーに設定する項目を変更しました。");
    }
}
