package net.kunmc.lab.hitpointssyncplugin.commands;

import net.kunmc.lab.hitpointssyncplugin.HitPointsSyncPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.md_5.bungee.api.ChatColor;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Bukkit;
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
                sender.sendMessage(genHelpText("maxhp", "プレイヤの最大HPをセットします。"));
                sender.sendMessage(genHelpText("healspeed", "1分間に回復するハートの数をセットします。"));
                break;
            case "maxhp":
                MaxHPCommand.maxhp(sender, (String[]) ArrayUtils.remove(args, 0));
                break;
            case "register":
                RegisterCommand.register(sender, (String[]) ArrayUtils.remove(args, 0));
                break;
            case "healspeed":
                    HealSpeed.healspeed(sender, (String[]) ArrayUtils.remove(args, 0));
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
            case 0:
                result.addAll(Arrays.asList("help", "maxhp", "register", "healspeed"));
                break;
            case 1:
                switch (args[0])
                {
                    case "register":
                        result.addAll(Bukkit.getScoreboardManager().getMainScoreboard().getTeams().stream()
                                .map(Team::getName)
                                .collect(Collectors.toList()));
                        break;
                    case "maxhp":
                    case "healspeed":
                        result.addAll(HitPointsSyncPlugin.managers.keySet());
                        result.add("all");
                        result.add("*");
                        break;
                }
        }


        ArrayList<String> asCopy = new ArrayList<>();
        StringUtil.copyPartialMatches(args[args.length - 1], result, asCopy);
        Collections.sort(asCopy);
        return asCopy;
    }
}
