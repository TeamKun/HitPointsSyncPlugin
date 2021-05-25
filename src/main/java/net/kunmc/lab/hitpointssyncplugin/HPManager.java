package net.kunmc.lab.hitpointssyncplugin;

import net.kyori.adventure.text.Component;
import net.minecraft.server.v1_16_R3.EntityPlayer;
import net.minecraft.server.v1_16_R3.PacketPlayOutAnimation;
import net.minecraft.server.v1_16_R3.PlayerConnection;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class HPManager
{
    private static Scoreboard mainScoreboard;

    private final String name;

    private final Team team;

    private int maxHP;

    private int regenPerMinute;

    static
    {
        mainScoreboard = Bukkit.getServer().getScoreboardManager().getMainScoreboard();
    }

    public HPManager(Team team)
    {
        if (team == null)
        {
            this.name = "main";
            if (mainScoreboard.getTeam(name) == null)
                this.team = mainScoreboard.registerNewTeam(name);
            else
                this.team = mainScoreboard.getTeam(name);
        }
        else
        {
            this.name = team.getName();
            this.team = team;
        }

        this.maxHP = 20;
    }

    public int getMaxHP()
    {
        return maxHP;
    }

    public void setMaxHP(int maxHP)
    {
        team.getEntries()
                .forEach(s -> {
                    Player player = Bukkit.getPlayer(s);
                    if (player != null)
                        player.getAttribute(Attribute.GENERIC_MAX_HEALTH)
                                .setBaseValue(maxHP);
                });
        this.maxHP = maxHP;
    }

    public void applyDamage(Player damager, EntityDamageEvent.DamageCause cause) // damager => 戦犯
    {
        damager.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 100, 1));

        String message = damager.getName() + "は" + Utils.toMessage(damager, cause) + "ダメージを負った";

        team.getEntries()
                .forEach(s -> {
                    Player player = Bukkit.getPlayer(s);
                    if (player == null)
                        return;
                    notification(player, message);
                });
    }

    private static void notification(Player player, String message)
    {
        player.sendActionBar(Component.text(ChatColor.RED + message));

        EntityPlayer playerEntity = ((CraftPlayer) player).getHandle();

        PlayerConnection connection = playerEntity.playerConnection;

        connection.sendPacket(new PacketPlayOutAnimation(playerEntity, 1));
    }

    public int getRegenPerMinute()
    {
        return regenPerMinute;
    }

    public void setRegenPerMinute(int regenPerMinute)
    {
        this.regenPerMinute = regenPerMinute;
    }
}
