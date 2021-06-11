package net.kunmc.lab.hitpointssyncplugin.commands;

import net.kunmc.lab.hitpointssyncplugin.HPManager;
import net.kunmc.lab.hitpointssyncplugin.HitPointsSyncPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class MainCommand implements CommandExecutor, TabCompleter
{
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if (!sender.hasPermission("hpsync.admin"))
        {
            sender.sendMessage(Component.text(ChatColor.RED + "エラー：あなたには権限がありません。"));
            return true;
        }

        if (args.length == 0)
        {
            sender.sendMessage(Component.text(ChatColor.RED + "エラー：引数が足りません！/hpsync help を利用してください。"));
            return true;
        }

        switch (args[0])
        {
            case "help":
                sender.sendMessage(genHelpText("help", "このコマンドです。"));
                sender.sendMessage(genHelpText("start", "ゲームを開始します。"));
                sender.sendMessage(genHelpText("stop", "ゲームをストップします。"));
                sender.sendMessage(genHelpText("maxhp", "プレイヤの最大HPをセットします。"));
                sender.sendMessage(genHelpText("healspeed", "何秒(小数可)に1HP回復するか"));
                sender.sendMessage(genHelpText("register", "追跡するチームに追加します。"));
                sender.sendMessage(genHelpText("ranking", "ランキング関連のコマンドです。"));
                sender.sendMessage(genHelpText("regenlimit", "1分間に回復する量を定義します。-1で無制限です。"));
                break;
            case "maxhp":
                MaxHPCommand.maxhp(sender, (String[]) ArrayUtils.remove(args, 0));
                break;
            case "register":
                RegisterCommand.register(sender, (String[]) ArrayUtils.remove(args, 0));
                break;
            case "healspeed":
                HealSpeed.healspeed(sender, (String[]) ArrayUtils.remove(args, 0));
                break;
            case "heallimit":
                HealLimitCommand.regenpermt(sender, (String[]) ArrayUtils.remove(args, 0));
                break;
            case "start":
                if (HitPointsSyncPlugin.started)
                {
                    sender.sendMessage(ChatColor.RED + "エラー：既に開始しています。");
                    return true;
                }

                HitPointsSyncPlugin.started = true;
                HitPointsSyncPlugin.managers.forEach((s, hpManager) -> {
                    HitPointsSyncPlugin.activeManagers.add(s);
                    hpManager.start();
                });
                sender.sendMessage(ChatColor.GREEN + "ゲームを開始しました。");
                break;
            case "stop":
                if (!HitPointsSyncPlugin.started)
                {
                    sender.sendMessage(ChatColor.RED + "エラー：まだ開始していません。");
                    return true;
                }

                HitPointsSyncPlugin.managers.values().forEach(HPManager::stop);
                HitPointsSyncPlugin.started = false;
                sender.sendMessage(ChatColor.GREEN + "ゲームをストップしました。");
                break;
            case "ranking":
                RankingCommand.ranking(sender, (String[]) ArrayUtils.remove(args, 0));
                break;
            case "regenlimit":
                HealLimitCommand.regenpermt(sender, (String[]) ArrayUtils.remove(args, 0));
                break;
            default:
                sender.sendMessage(ChatColor.RED + "エラー：不明な引数です。/hpsync help をご利用ください。");

        }

        return true;
    }

    private static TextComponent genHelpText(String commandName, String description)
    {
        return Component.text(
                ChatColor.GREEN + "/hpsync " + commandName +
                ChatColor.WHITE + " - " +
                ChatColor.LIGHT_PURPLE + description)
                .clickEvent(ClickEvent.suggestCommand("/hpsync " + commandName + " "));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args)
    {
        ArrayList<String> result = new ArrayList<>();

        if (!sender.hasPermission("hpsync.admin"))
            return null;

        switch (args.length)
        {
            case 1:
                result.addAll(Arrays.asList("help", "maxhp", "register", "healspeed", "start", "stop", "ranking", "heallimit"));
                break;
            case 2:
                switch (args[0])
                {
                    case "register":
                        result.addAll(Bukkit.getScoreboardManager().getMainScoreboard().getTeams().stream()
                                .map(Team::getName)
                                .collect(Collectors.toList()));
                        break;
                    case "maxhp":
                    case "healspeed":
                    case "heallimit":
                        result.addAll(HitPointsSyncPlugin.managers.keySet());
                        result.add("all");
                        result.add("*");
                        break;
                    case "ranking":
                        result.add("damage");
                        result.add("none");
                        result.add("clear");
                        break;
                }
        }


        ArrayList<String> asCopy = new ArrayList<>();
        StringUtil.copyPartialMatches(args[args.length - 1], result, asCopy);
        Collections.sort(asCopy);
        return asCopy;
    }
}
