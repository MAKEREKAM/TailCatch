package kr.vanilage.tailcatch;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;

public class DrawParticle implements Listener {
    static void drawLine(Location point1, Location point2, double space) {
        World world = point1.getWorld();

        double distance = point1.distance(point2);

        Vector p1 = point1.toVector();
        Vector p2 = point2.toVector();

        Vector vector = p2.clone().subtract(p1).normalize().multiply(space);

        double covered = 0;

        for (; covered < distance; p1.add(vector)) {
            world.spawnParticle(Particle.FLAME, p1.getX(), p1.getY(), p1.getZ(), 10, 0, 0, 0, 0, null);

            covered += space;
        }
    }
}
