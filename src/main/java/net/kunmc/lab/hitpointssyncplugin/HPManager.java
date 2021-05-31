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
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.Objects;

public class HPManager
{
    private static Scoreboard mainScoreboard;

    private final String name;
    private final Team team;
    private Runnable healTimer;

    private double maxHP;
    private int regenAmount;
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

        healTimer = () -> {
            if (this.healing)
            {
                if (this.nowHP >= this.maxHP)
                {
                    this.healing = false;
                    return;
                }

                nowHP++;
                this.team.getEntries().stream()
                        .map(Bukkit::getPlayer)
                        .filter(Objects::nonNull)
                        .forEach(player -> {
                            player.setHealth(nowHP);
                        });
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

        healTimerId = Bukkit.getScheduler().scheduleSyncRepeatingTask(HitPointsSyncPlugin.instance, healTimer, 0L, regenAmount);
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
        Bukkit.getScheduler().cancelTask(healTimerId);

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
    }
}
