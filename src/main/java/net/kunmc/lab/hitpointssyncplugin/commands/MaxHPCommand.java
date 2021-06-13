package net.kunmc.lab.hitpointssyncplugin.commands;

import net.kunmc.lab.hitpointssyncplugin.HitPointsSyncPlugin;
import net.kunmc.lab.hitpointssyncplugin.Utils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class MaxHPCommand
{
    public static void maxhp(CommandSender sender, String[] args)
    {
        switch (args.length)
        {
            case 0:
            case 1:
                sender.sendMessage(ChatColor.GREEN + "現在の設定値：");

                HitPointsSyncPlugin.managers.forEach((s, hpManager) -> {
                    sender.sendMessage(ChatColor.GREEN + "    " + s + "：" + hpManager.getMaxHP());
                });

                sender.sendMessage(ChatColor.GRAY + "コマンド使用法：/hpsync maxhp <チーム名> <最大HP(ハート数)>") ;
                return;
            /*case 1:
                Double num = Utils.parseDouble(args[0]);
                if (num == null)
                    sender.sendMessage(ChatColor.RED + "エラー：引数が数字ではありません。");
                else
                {
                    if (num >= 1024)
                    {
                        sender.sendMessage(ChatColor.RED + "HPは1024以下である必要があります。");
                        return;
                    }

                    HitPointsSyncPlugin.managers.get("main").setMaxHP(num * 2);
                    sender.sendMessage(ChatColor.GREEN + "メインチームのプレイヤの最大HPを " + num + " にセットしました。");
                }
                return;*/
            case 2:
                Double num = Utils.parseDouble(args[1]);
                if (num == null)
                    sender.sendMessage(ChatColor.RED + "エラー：引数が数字ではありません。");
                else
                {
                    if (num >= 1024)
                    {
                        sender.sendMessage(ChatColor.RED + "HPは1024以下である必要があります。");
                        return;
                    }

                    if (args[0].equals("*") || args[0].equals("all"))
                    {
                        HitPointsSyncPlugin.managers.values()
                                .forEach(hpManager -> {
                                    hpManager.setMaxHP(num * 2);
                                });
                        sender.sendMessage(ChatColor.GREEN + "全てのチームのプレイヤの最大HPを " + num + " にセットしました。");
                        return;
                    }

                    if (!HitPointsSyncPlugin.managers.containsKey(args[0]))
                    {
                        sender.sendMessage(ChatColor.RED + "エラー：チームが見つかりませんでした。");
                        return;
                    }

                    HitPointsSyncPlugin.managers.get(args[0]).setMaxHP(num);
                    sender.sendMessage(ChatColor.GREEN + "対象チームのプレイヤの最大HPを " + num + " にセットしました。");
                }
        }

    }
}
