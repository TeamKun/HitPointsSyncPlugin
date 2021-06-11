package net.kunmc.lab.hitpointssyncplugin.commands;

import net.kunmc.lab.hitpointssyncplugin.HitPointsSyncPlugin;
import net.kunmc.lab.hitpointssyncplugin.Utils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class HealLimitCommand
{
    public static void regenpermt(CommandSender sender, String[] args)
    {
        switch (args.length)
        {
            case 0:
                sender.sendMessage(ChatColor.GREEN + "現在の設定値：");

                HitPointsSyncPlugin.managers.forEach((s, hpManager) -> {
                    sender.sendMessage(ChatColor.GREEN + "    " + s + "：" + hpManager.getRegenPerMinute());
                });

                sender.sendMessage(ChatColor.GRAY + "コマンド使用法使用法：/hpsync regenlimit <チーム名> <1分間に回復できる数(❤)>");
                sender.sendMessage(ChatColor.GRAY + "ノート：1分間に回復できる数を-1にセットすると無制限になります。");
                return;
            /*case 1:
                Integer num = Utils.parseInt(args[0]);
                if (num == null)
                    sender.sendMessage(ChatColor.RED + "エラー：引数が数字ではありません。");
                else
                {
                    HitPointsSyncPlugin.managers.get("main").setRegenPerMinute(num);
                    sender.sendMessage(ChatColor.GREEN + "メインチームの回復制限を " + num + " にセットしました。");
                }
                return;*/
            case 2:
                Integer num = Utils.parseInt(args[1]);
                if (num == null)
                    sender.sendMessage(ChatColor.RED + "エラー：引数が数字ではありません。");
                else
                {
                    if (args[0].equals("*") || args[0].equals("all"))
                    {
                        HitPointsSyncPlugin.managers.values()
                                .forEach(hpManager -> {
                                    hpManager.setRegenPerMinute(num);
                                });
                        sender.sendMessage(ChatColor.GREEN + "全てのチームの回復制限を " + num + " にセットしました。");
                        return;
                    }

                    if (!HitPointsSyncPlugin.managers.containsKey(args[0]))
                    {
                        sender.sendMessage(ChatColor.RED + "エラー：チームが見つかりませんでした。");
                        return;
                    }

                    HitPointsSyncPlugin.managers.get(args[0]).setRegenPerMinute(num);
                    sender.sendMessage(ChatColor.GREEN + "対象チームの回復制限を " + num + " にセットしました。");
                }
        }
    }
}
