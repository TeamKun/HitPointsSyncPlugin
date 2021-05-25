package net.kunmc.lab.hitpointssyncplugin.commands;

import net.kunmc.lab.hitpointssyncplugin.HitPointsSyncPlugin;
import net.kunmc.lab.hitpointssyncplugin.Utils;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;

public class MaxHPCommand
{
    public static void maxhp(CommandSender sender, String[] args)
    {
        switch (args.length)
        {
            case 0:
                sender.sendMessage(ChatColor.RED + "エラー：使用法が間違っています。使用法：/hpsync maxhp [チーム名] <最大HP(ハート数)>") ;
                return;
            case 1:
                Integer num = Utils.parseInt(args[0]);
                if (num == null)
                    sender.sendMessage(ChatColor.RED + "エラー：引数が数字ではありません。");
                else
                {
                    HitPointsSyncPlugin.managers.get("main").setMaxHP(num);
                    sender.sendMessage(ChatColor.GREEN + "メインチームのプレイヤの最大HPを " + num + " にセットしました。");
                }
                return;
            case 2:
                num = Utils.parseInt(args[1]);
                if (num == null)
                    sender.sendMessage(ChatColor.RED + "エラー：引数が数字ではありません。");
                else
                {
                    if (args[0].equals("*") || args[0].equals("all"))
                    {
                        HitPointsSyncPlugin.managers.values()
                                .forEach(hpManager -> {
                                    hpManager.setMaxHP(num);
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
