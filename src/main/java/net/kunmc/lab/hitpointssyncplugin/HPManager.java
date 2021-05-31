package net.kunmc.lab.hitpointssyncplugin;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.EntityEffect;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.Objects;

public class HPManager
{
    private static Scoreboard mainScoreboard;

    private final String name;
    private final Team team;
    private BukkitRunnable healTimer;

    private double maxHP;
    private int regenAmount;
    private double nowHP;

    private boolean started;
    private volatile boolean healing;

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
        this.regenAmount = 20;
        this.started = false;

        healTimer = new BukkitRunnable()
        {
            @Override
            public void run()
            {
                if (healing)
                {
                    nowHP++;
                    team.getEntries().stream()
                            .map(Bukkit::getPlayer)
                            .filter(Objects::nonNull)
                            .forEach(player -> {
                                player.setHealth(nowHP);
                            });
                }
            }
        };
    }

    public void start()
    {
        this.team.getEntries().stream()
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .forEach(player -> {
                    player.setHealth(this.maxHP);
                    player.getAttribute(Attribute.GENERIC_MAX_HEALTH)
                            .setBaseValue(this.maxHP);
                });


        this.nowHP = this.maxHP;

        healTimer.runTaskTimer(HitPointsSyncPlugin.instance, 0L, regenAmount);
        this.started = true;
    }

    public boolean isStarted()
    {
        return started;
    }

    public double getMaxHP()
    {
        return maxHP;
    }

    public void setMaxHP(double maxHP)
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
        damager.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 100, 1));

        String message = damager.getName() + "は" + Utils.toMessage(damager, cause) + "ダメージを負った。";
        Ranking.push(Ranking.Mode.DAMAGE, damager.getName(), (int) Math.round(amount));
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
        this.healTimer.cancel();
        HitPointsSyncPlugin.activeManagers.remove(name);
    }

    class HealRunnable extends BukkitRunnable
    {
        public volatile int time = 0;
        @Override
        public void run()
        {
            if (time > 3)
                healing = false;
        }
    }

    public HealRunnable healRunnable = new HealRunnable();

    public boolean regen(String regenner)
    {
        if (!this.started)
              return false;
        if (healing)
        {
            healRunnable.time = 0;
            return true;
        }
        healing = true;

        healRunnable.runTaskTimer(HitPointsSyncPlugin.instance, 0L, 20L);

        return true;
    }


    private static void notification(Player player, String message)
    {
        player.sendActionBar(Component.text(ChatColor.RED + message));

        player.playEffect(EntityEffect.HURT);
    }

    public void setRegenAmount(double regenAmount)
    {
        this.regenAmount = (int) (regenAmount * 20d);
    }
}
