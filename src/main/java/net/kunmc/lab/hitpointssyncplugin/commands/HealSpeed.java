package net.kunmc.lab.hitpointssyncplugin.commands;

import net.kunmc.lab.hitpointssyncplugin.HitPointsSyncPlugin;
import net.kunmc.lab.hitpointssyncplugin.Utils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class HealSpeed
{
    public static void healspeed(CommandSender sender, String[] args)
    {
        switch (args.length)
        {
            case 0:
                sender.sendMessage(ChatColor.RED + "エラー：使用法が間違っています。使用法：/hpsync healspeed [チーム名] <n秒に1回回復>") ;
                return;
            case 1:
                Double num = Utils.parseDouble(args[0]);
                if (num == null)
                    sender.sendMessage(ChatColor.RED + "エラー：引数が数字ではありません。");
                else
                {
                    HitPointsSyncPlugin.managers.get("main").setRegenAmount(num);
                    sender.sendMessage(ChatColor.GREEN + "メインチームの回復レートを " + num + " にセットしました。");
                }
                return;
            case 2:
                num = Utils.parseDouble(args[0]);
                if (num == null)
                    sender.sendMessage(ChatColor.RED + "エラー：引数が数字ではありません。");
                else
                {
                    if (args[0].equals("*") || args[0].equals("all"))
                    {
                        HitPointsSyncPlugin.managers.values()
                                .forEach(hpManager -> {
                                    hpManager.setRegenAmount(num);
                                });
                        sender.sendMessage(ChatColor.GREEN + "全てのチームの回復レートを " + num + " にセットしました。");
                        return;
                    }

                    if (!HitPointsSyncPlugin.managers.containsKey(args[0]))
                    {
                        sender.sendMessage(ChatColor.RED + "エラー：チームが見つかりませんでした。");
                        return;
                    }

                    HitPointsSyncPlugin.managers.get(args[0]).setRegenAmount(num);
                    sender.sendMessage(ChatColor.GREEN + "対象チームの回復レートを " + num + " にセットしました。");
                }
        }
    }
}
