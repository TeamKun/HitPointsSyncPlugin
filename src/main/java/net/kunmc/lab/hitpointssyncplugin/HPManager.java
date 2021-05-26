package net.kunmc.lab.hitpointssyncplugin;

import net.kyori.adventure.text.Component;
import net.minecraft.server.v1_16_R3.EntityPlayer;
import net.minecraft.server.v1_16_R3.PacketPlayOutAnimation;
import net.minecraft.server.v1_16_R3.PlayerConnection;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.EntityEffect;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.Objects;

public class HPManager
{
    private static Scoreboard mainScoreboard;

    private final String name;
    private final Team team;
    private BossBar bar;

    private int maxHP;
    private int regenPerMinute;
    private int regendHP;
    private double nowHP;

    private boolean started;

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
        this.nowHP = this.maxHP;
        this.regendHP = 0;
        this.started = false;
        this.bar = Bukkit.createBossBar("残り回復可能HP", BarColor.GREEN, BarStyle.SOLID);
    }

    public void start()
    {
        this.team.getEntries().stream()
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .forEach(player -> {
                    this.bar.addPlayer(player);
                    player.setHealth(this.maxHP);
                    player.getAttribute(Attribute.GENERIC_MAX_HEALTH)
                            .setBaseValue(this.maxHP);
                });

        this.bar.setVisible(true);
        if (getRegenPerMinute() == 0)
            this.bar.setProgress(0.0);
        else
            this.bar.setProgress(1.0);

        this.nowHP = this.maxHP;

        this.started = true;
    }

    public boolean isStarted()
    {
        return started;
    }

    public int getMaxHP()
    {
        return maxHP;
    }

    public void setMaxHP(int maxHP)
    {
        this.nowHP = maxHP;
        team.getEntries()
                .forEach(s -> {
                    Player player = Bukkit.getPlayer(s);
                    if (player != null)
                    {
                        player.getAttribute(Attribute.GENERIC_MAX_HEALTH)
                                .setBaseValue(maxHP);
                        player.setHealth(this.nowHP);
                    }
                });
        this.maxHP = maxHP;
    }

    public void applyDamage(Player damager, EntityDamageEvent.DamageCause cause, double amount) // damager => 戦犯
    {
        /*EntityPlayer playerEntity = ((CraftPlayer) damager).getHandle();

        PlayerConnection connection = playerEntity.playerConnection;

        connection.sendPacket(new PacketPlayOutAnimation(playerEntity, 1));*/
        damager.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 100, 1));

        String message = damager.getName() + "は" + Utils.toMessage(damager, cause) + "ダメージを負った。";

        if (nowHP - amount <= 0)
        {
            team.getEntries()
                    .forEach(s -> {
                        Player player = Bukkit.getPlayer(s);
                        if (player == null)
                            return;
                        player.setHealth(0);
                    });

            this.nowHP = 0;

            Bukkit.getOnlinePlayers().forEach(player -> player.sendMessage(ChatColor.RED + team.getName() + " チームは、" + damager.getName() + "が負ったダメージで全滅した。"));
            stop();
        }

        this.nowHP = nowHP - amount;

        team.getEntries()
                .forEach(s -> {
                    Player player = Bukkit.getPlayer(s);
                    if (player == null)
                        return;
                    if (this.nowHP < 0)
                        return;

                    player.setHealth(this.nowHP);
                    notification(player, message);
                });
    }

    public void stop()
    {
        this.started = false;
        this.bar.setVisible(false);
        this.bar.removeAll();
        this.regendHP = 0;
    }

    public boolean regen(double amount)
    {
        if (!this.started || this.regendHP + amount > this.regenPerMinute)
              return false;

        double regenAmount = amount;

        if (this.nowHP + amount > this.maxHP)
        {
            this.regendHP += this.maxHP - this.nowHP;
            regenAmount = this.maxHP - this.nowHP;
        }
        else
            this.regendHP += amount;

        double finalRegenAmount = regenAmount;
        team.getEntries()
                .forEach(s -> {
                    Player player = Bukkit.getPlayer(s);
                    if (player != null)
                        player.setHealth(this.nowHP + finalRegenAmount);
                });

        this.nowHP = this.nowHP + finalRegenAmount;
        this.bar.setProgress(1.0 - ((double) regendHP / (double) regenPerMinute));
        return true;
    }


    private static void notification(Player player, String message)
    {
        player.sendActionBar(Component.text(ChatColor.RED + message));

        player.playEffect(EntityEffect.HURT);
    }

    public int getRegenPerMinute()
    {
        return regenPerMinute;
    }

    public void setRegenPerMinute(int regenPerMinute)
    {
        double progress = this.bar.getProgress() * this.regenPerMinute;

        this.regenPerMinute = regenPerMinute;

        if (this.bar.getProgress() == 0.0 || regenPerMinute == 0.0)
            return;

        this.bar.setProgress(progress / (double) regenPerMinute);
    }
}
