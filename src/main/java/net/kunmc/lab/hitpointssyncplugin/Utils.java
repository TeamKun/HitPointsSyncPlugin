package net.kunmc.lab.hitpointssyncplugin;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scoreboard.Team;

public class Utils
{
    public static HPManager getManager(Player player)
    {
        Team team = player.getScoreboard().getEntryTeam(player.getName());
        if (team == null)
            return  HitPointsSyncPlugin.managers.get("main");
        else
            return  HitPointsSyncPlugin.managers.get(team.getName());
    }

    public static Integer parseInt(String str)
    {
        try
        {
            return Integer.parseInt(str);
        }
        catch (Exception ignored)
        {
            return null;
        }
    }
    public static Double parseDouble(String str)
    {
        try
        {
            return Double.valueOf(str);
        }
        catch (Exception ignored)
        {
            return null;
        }
    }

    public static String toMessage(Player damager, EntityDamageEvent.DamageCause cause)
    {
        switch (cause)
        {
            case BLOCK_EXPLOSION:
                return "は爆発により";
            case CRAMMING:
            case DROWNING:
            case SUFFOCATION:
                return "は窒息により";
            case DRAGON_BREATH:
                return "はドラゴンにより";
            case ENTITY_ATTACK:
                EntityDamageEvent e = damager.getLastDamageCause();
                if (e != null && !(e instanceof EntityDamageByEntityEvent))
                    return "";
                EntityDamageByEntityEvent evt =
                        (EntityDamageByEntityEvent) damager.getLastDamageCause();
                return /*evt.getDamager().getName() + */"殴られて";
            case ENTITY_EXPLOSION:
                return "クリーパーの爆発で";
            case FALL:
                return "落下の衝撃で";
            case FALLING_BLOCK:
                return "ブロックが堕ちてきて";
            case FIRE:
            case FIRE_TICK:
                return "火で";
            case FLY_INTO_WALL:
                return "壁にぶつかって";
            case HOT_FLOOR:
                return "熱いい床で";
            case LAVA:
                return "溶岩で";
            case LIGHTNING:
                return "雷で";
            case MAGIC:
                return "魔法で";
            case POISON:
                return "毒の作用で";
            case PROJECTILE:
                return "弓で撃たれて";
            case STARVATION:
                return "空腹で";
            case VOID:
                return "奈落で";
            case WITHER:
                return "ウィザーの効果で";
        }

        return "";
    }
}
