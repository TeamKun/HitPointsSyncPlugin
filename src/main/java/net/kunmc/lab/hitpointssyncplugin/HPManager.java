package net.kunmc.lab.hitpointssyncplugin;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.EntityEffect;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
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
    private Runnable healTimer;
    private BossBar bar;

    private double maxHP;
    private int regenAmount;
    private int regendHP;
    private int regenPerMinute;
    private Runnable healLimitTimer;
    private int healLimitTimerid;
    private double nowHP;
    private int healTimerId = -1;


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
        this.regenPerMinute = -1;
        this.bar = Bukkit.createBossBar("残り回復可能HP", BarColor.GREEN, BarStyle.SOLID);
        healTimer = () -> {
            if (this.healing)
            {
                if ((this.regendHP != -1 && this.regendHP > this.regenPerMinute) || this.nowHP >= this.maxHP)
                {
                    this.healing = false;
                    return;
                }

                nowHP++;
                regendHP++;
                this.team.getEntries().stream()
                        .map(Bukkit::getPlayer)
                        .filter(Objects::nonNull)
                        .forEach(player -> {
                            player.setHealth(nowHP);
                        });

                this.bar.setProgress(1.0 - ((double) regendHP / (double) regenPerMinute));
            }
        };

        healLimitTimer = () -> {
            regendHP = 0;
            bar.setProgress(1.0);
        };
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

        if (regenPerMinute != -1)
            this.bar.setVisible(true);
        if (getRegenPerMinute() < 0)
            this.bar.setProgress(0.0);
        else
            this.bar.setProgress(1.0);
        this.nowHP = this.maxHP;

        healTimerId = Bukkit.getScheduler().scheduleSyncRepeatingTask(HitPointsSyncPlugin.instance, healTimer, 0L, regenAmount);
        healLimitTimerid = Bukkit.getScheduler().scheduleSyncRepeatingTask(HitPointsSyncPlugin.instance, healLimitTimer, 0L, 1200L);
        this.started = true;
    }

    public int getRegenPerMinute()
    {
        return regenPerMinute;
    }

    public void setRegenPerMinute(int regenPerMinute)
    {
        this.regenPerMinute = regenPerMinute;
        this.regendHP = 0;
        this.bar.setProgress(1.0);
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
        if (Bukkit.getScheduler().isQueued(healTimerId))
            Bukkit.getScheduler().cancelTask(healTimerId);
        if (Bukkit.getScheduler().isQueued(healRunnableId))
            Bukkit.getScheduler().cancelTask(healRunnableId);
        if (Bukkit.getScheduler().isQueued(healLimitTimerid))
            Bukkit.getScheduler().cancelTask(healLimitTimerid);

        this.bar.setVisible(false);
        this.bar.removeAll();
        this.regendHP = 0;

        HitPointsSyncPlugin.activeManagers.remove(name);
        if (HitPointsSyncPlugin.activeManagers.size() == 0)
        {
            Bukkit.getOnlinePlayers().forEach(player -> {
                player.sendMessage(ChatColor.RED + "全てのチームが脱落したためゲームが終了しました。");
            });
            HitPointsSyncPlugin.started = false;
        }
    }

    class HealRunnable implements Runnable
    {
        public int time = 0;
        @Override
        public void run()
        {
            if (time > 3)
            {
                healing = false;
                time = 0;
                Bukkit.getScheduler().cancelTask(healRunnableId);
            }
            else
                time++;
        }


    }

    public HealRunnable healRunnable = new HealRunnable();
    public int healRunnableId = -1;

    public void regen()
    {
        if (!this.started)
              return;
        if (healing)
        {
            healRunnable.time = 0;
            return;
        }
        healing = true;

        healRunnableId = Bukkit.getScheduler().scheduleSyncRepeatingTask(HitPointsSyncPlugin.instance, healRunnable, 0L, 40L);

    }


    private static void notification(Player player, String message)
    {
        player.sendActionBar(Component.text(ChatColor.RED + message));

        player.playEffect(EntityEffect.HURT);
    }

    public void setRegenAmount(double regenAmount)
    {
        this.regenAmount = (int) (regenAmount * 20d);

        if (!started)
            return;

        if (Bukkit.getScheduler().isQueued(healTimerId))
            Bukkit.getScheduler().cancelTask(healTimerId);
        healTimerId = Bukkit.getScheduler().scheduleSyncRepeatingTask(HitPointsSyncPlugin.instance, healTimer, 0L, this.regenAmount);
    }

    public int getRegenAmount()
    {
        return regenAmount;
    }
}
