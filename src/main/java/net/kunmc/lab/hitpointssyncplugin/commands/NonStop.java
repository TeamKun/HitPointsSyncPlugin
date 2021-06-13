package net.kunmc.lab.hitpointssyncplugin.commands;

import net.kunmc.lab.hitpointssyncplugin.HitPointsSyncPlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class NonStop
{
    public static void nonStop(CommandSender sender, String[] args)
    {
        if (args.length != 2)
        {
            sender.sendMessage(ChatColor.GREEN + "現在の設定値：");

            HitPointsSyncPlugin.managers.forEach((s, hpManager) -> {
                sender.sendMessage(ChatColor.GREEN + "    " + s + "：" + (hpManager.isNonStop() ? "有効": "無効"));
            });

            sender.sendMessage(ChatColor.GRAY + "コマンド使用法：/hpsync nonstop <チーム名> <true|false>");
            return;
        }

        boolean isNonstop = Boolean.parseBoolean(args[1]);


        if (args[0].equals("*") || args[0].equals("all"))
        {
            HitPointsSyncPlugin.managers.values()
                    .forEach(hpManager -> {
                        hpManager.setNonStop(isNonstop);
                    });
            sender.sendMessage(ChatColor.GREEN + "全てのチームの死亡時継続モードを " + (isNonstop ? "有効化": "無効化") + "しました。");
            return;
        }

        if (!HitPointsSyncPlugin.managers.containsKey(args[0]))
        {
            sender.sendMessage(ChatColor.RED + "エラー：チームが見つかりませんでした。");
            return;
        }

        HitPointsSyncPlugin.managers.get(args[0]).setNonStop(isNonstop);
        sender.sendMessage(ChatColor.GREEN + "対象のチームの死亡時継続モードを " + (isNonstop ? "有効化": "無効化") + "しました。");
    }
}
