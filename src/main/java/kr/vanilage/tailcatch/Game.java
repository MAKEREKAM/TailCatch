package kr.vanilage.tailcatch;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.*;

public class Game implements Listener {
    static HashMap<UUID, Teams> teamUUID = new HashMap<>();
    static HashMap<Teams, ArrayList<UUID>> team = new HashMap<>();
    static ArrayList<Teams> remaining = new ArrayList<>();
    static HashMap<Teams, ChatColor> teamColor = new HashMap<>();

    public static void gameStart() {
        if (Bukkit.getOnlinePlayers().size() != 6) {
            return;
        }

        teamColor.put(Teams.RED, ChatColor.RED);
        teamColor.put(Teams.BLUE, ChatColor.BLUE);
        teamColor.put(Teams.GREEN, ChatColor.GREEN);
        teamColor.put(Teams.YELLOW, ChatColor.YELLOW);
        teamColor.put(Teams.PINK, ChatColor.LIGHT_PURPLE);
        teamColor.put(Teams.GRAY, ChatColor.GRAY);

        remaining.add(Teams.RED);
        remaining.add(Teams.BLUE);
        remaining.add(Teams.GREEN);
        remaining.add(Teams.YELLOW);
        remaining.add(Teams.PINK);
        remaining.add(Teams.GRAY);

        List<Teams> teamValue = Arrays.asList(Teams.values());
        int count = 0;

        for (Player i : Bukkit.getOnlinePlayers()) {
            teamUUID.put(i.getUniqueId(), teamValue.get(count));
            ArrayList<UUID> uuids = new ArrayList<>();
            uuids.add(i.getUniqueId());
            team.put(teamValue.get(count), uuids);
            count++;

            Random rd = new Random();
            int x = rd.nextInt(-1000, 1001);
            int z = rd.nextInt(-1000, 1001);
            int y = Bukkit.getWorld("world").getHighestBlockYAt(x, z);
            Location rdSpawn = new Location(Bukkit.getWorld("world"), x, y, z);
            i.teleport(rdSpawn);
        }
    }

    @EventHandler
    private void onDamage(EntityDamageEvent e) {
        if (!e.getEntity().getType().equals(EntityType.PLAYER)) return;
        if (!e.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_ATTACK)) e.setCancelled(true);
    }

    @EventHandler
    private void onDamageByEntity(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        if (!(e.getDamager() instanceof Player)) e.setCancelled(true);
        if (team.isEmpty()) {
            e.setCancelled(true);
            return;
        }

        Player victim = (Player) e.getEntity();
        Player attacker = (Player) e.getDamager();

        if (!((remaining.indexOf(teamUUID.get(victim.getUniqueId())) + 1)
                        == (remaining.indexOf(teamUUID.get(attacker.getUniqueId()))))) {
            e.setCancelled(true);
        }

        if (remaining.indexOf(teamUUID.get(victim)) == remaining.size() - 1 &&
                        remaining.indexOf(teamUUID.get(attacker)) == 0) {
            e.setCancelled(false);
        }
    }

    @EventHandler
    private void onDeath(PlayerDeathEvent e) {
        ArrayList<UUID> teamList = team.get(teamUUID.get(e.getEntity().getUniqueId()));
        if (teamList.get(1).equals(e.getEntity().getUniqueId())) {
            if (remaining.indexOf(teamUUID.get(e.getEntity().getUniqueId())) == remaining.size() - 1) {
                ArrayList<UUID> enemy = team.get(remaining.get(0));
                for (UUID i : teamList) {
                    enemy.add(i);
                }
                team.remove(remaining.get(0));
                team.put(remaining.get(0), enemy);
                team.remove(teamUUID.get(e.getEntity().getUniqueId()));
                remaining.remove(teamUUID.get(e.getEntity().getUniqueId()));
                for (UUID i : teamList) {
                    teamUUID.remove(i);
                    teamUUID.put(i, remaining.get(0));
                }

                for (Player i : Bukkit.getOnlinePlayers()) {
                    if (teamList.contains(i.getUniqueId())) {
                        i.setPlayerListName(teamColor.get(remaining.get(0)) + i.getName());
                    }
                }
            }

            else {
                int enemyIndex = remaining.indexOf(teamUUID.get(e.getEntity().getUniqueId())) + 1;
                ArrayList<UUID> enemy = team.get(remaining.get(enemyIndex));
                for (UUID i : teamList) {
                    enemy.add(i);
                }
                team.remove(remaining.get(enemyIndex));
                team.put(remaining.get(enemyIndex), enemy);
                team.remove(teamUUID.get(e.getEntity().getUniqueId()));
                remaining.remove(teamUUID.get(e.getEntity().getUniqueId()));
                for (UUID i : teamList) {
                    teamUUID.remove(i);
                    teamUUID.put(i, remaining.get(enemyIndex));
                }

                for (Player i : Bukkit.getOnlinePlayers()) {
                    if (teamList.contains(i.getUniqueId())) {
                        i.setPlayerListName(teamColor.get(remaining.get(enemyIndex)) + i.getName());
                    }
                }
            }

            if (remaining.size() == 1) {
                Bukkit.broadcastMessage(ChatColor.GREEN + "게임 종료.");
            }
        }
    }

    @EventHandler
    private void onRightClick(PlayerInteractEvent e) {
        if (e.getAction().equals(Action.RIGHT_CLICK_AIR)) {
            if (e.getPlayer().getInventory().getItemInMainHand() != null && e.getPlayer().getInventory().getItemInMainHand().getType().equals(Material.OBSIDIAN)) {
                int indexTeam = remaining.indexOf(teamUUID.get(e.getPlayer().getUniqueId()));
                if (indexTeam == 0) {
                    if (Bukkit.getPlayer(team.get(remaining.get(remaining.size() - 1)).get(0)) == null) {
                        e.getPlayer().sendMessage(ChatColor.RED + "플레이어가 서버에서 나갔습니다.");
                        return;
                    }

                    Player p = Bukkit.getPlayer(team.get(remaining.get(remaining.size() - 1)).get(0));
                    if (!e.getPlayer().getWorld().equals(p.getWorld())) {
                        e.getPlayer().sendMessage(ChatColor.RED + "플레이어가 다른 월드에 있습니다.");
                        return;
                    }

                    DrawParticle.drawLine(e.getPlayer().getLocation(), p.getLocation(), 0.1);
                }

                else {
                    if (Bukkit.getPlayer(team.get(remaining.get(indexTeam - 1)).get(0)) == null) {
                        e.getPlayer().sendMessage(ChatColor.RED + "플레이어가 서버에서 나갔습니다.");
                        return;
                    }

                    Player p = Bukkit.getPlayer(team.get(remaining.get(indexTeam - 1)).get(0));
                    if (!e.getPlayer().getWorld().equals(p.getWorld())) {
                        e.getPlayer().sendMessage(ChatColor.RED + "플레이어가 다른 월드에 있습니다.");
                        return;
                    }

                    DrawParticle.drawLine(e.getPlayer().getLocation(), p.getLocation(), 0.1);
                }

                e.getPlayer().getInventory().getItemInMainHand().setAmount(e.getPlayer().getInventory().getItemInMainHand().getAmount() - 1);
            }
        }
    }
}
