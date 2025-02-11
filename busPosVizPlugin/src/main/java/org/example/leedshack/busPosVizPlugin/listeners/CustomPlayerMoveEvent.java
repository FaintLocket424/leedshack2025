package org.example.leedshack.busPosVizPlugin.listeners;

import java.util.HashMap;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.example.leedshack.busPosVizPlugin.BusPosVizPlugin;
import org.example.leedshack.busPosVizPlugin.BusStopMC;

public class CustomPlayerMoveEvent implements Listener {

    public static void clearStops() {
        stopToStand.clear();
    }

//    private static final Set<ArmorStand> armorStands = new HashSet<>();

    private static final HashMap<BusStopMC, ArmorStand> stopToStand = new HashMap<>();
    private final int armorStandRange = 20;

    @EventHandler
    public void onPlayerMoveEvent(PlayerMoveEvent event) {
        if (!event.hasChangedPosition()) {
            return;
        }

        Player player = event.getPlayer();
        World world = player.getWorld();
        Location playerLoc = player.getLocation();
        if (BusPosVizPlugin.BusStopsMC == null) {
            return;
        }

        Predicate<Location> isNearPlayer = l ->
            l.distanceSquared(playerLoc) <= Math.pow(armorStandRange, 2);

        Set<BusStopMC> stopsNearPlayer = BusPosVizPlugin.BusStopsMC.stream()
            .filter(bsm -> isNearPlayer.test(bsm.loc()))
            .collect(Collectors.toSet());

        Set<BusStopMC> stopsOutOfRange = stopToStand.keySet().stream()
            .filter(bsm -> !stopsNearPlayer.contains(bsm))
            .collect(Collectors.toSet());

        // kill all the armor stands outside the range, then remove to armor stand.
        for (BusStopMC bsm : stopsOutOfRange) {
            stopToStand.get(bsm).remove();
            stopToStand.remove(bsm);
        }

        Set<BusStopMC> stopsNewlyNear = stopsNearPlayer.stream()
            .filter(bsm -> !stopToStand.containsKey(bsm))
            .collect(Collectors.toSet());

        for (BusStopMC bsm : stopsNewlyNear) {
            Location location = bsm.loc().clone();
            location.setY(location.getY() - 1.4);
            ArmorStand as = (ArmorStand) world.spawnEntity(
                location, EntityType.ARMOR_STAND
            );
            as.setInvisible(true);

            as.customName(
                Component.text(bsm.name()).color(TextColor.color(NamedTextColor.RED)).decorate(
                    TextDecoration.BOLD));
            as.setCustomNameVisible(true);
            as.setInvulnerable(true);
            as.setGravity(false);
            stopToStand.put(bsm, as);
        }

//        Set<BusStopMC> stopsNearPlayer = BusPosVizPlugin.BusStopsMC.stream()
//            .filter(bsm -> isNear.test(bsm.loc()))
//            .collect(Collectors.toSet());

        // get stops near player
        // kill and remove stops from main set that are not near player
        // add stops from main set that are NEWLY near to player

//
//
//
//
//
//        Stream<BusStopMC> stopsNearPlayer = BusPosVizPlugin.BusStopsMC.stream().filter(bsm -> isNear.test(bsm.loc()));
//
//
//        List<ArmorStand> armorStandsOutside = armorStands.stream()
//            .filter(as -> !isNear.test(as.getLocation())).toList();
//        armorStandsOutside.forEach(Entity::remove);
//
//        armorStands = armorStands.stream().filter(as -> !armorStandsOutside.contains(as)).toList();
//
//
//
//        stopsNearPlayer.forEach(x -> {
////           List<ArmorStand> playerStands = armourStands.getOrDefault(player,
////               new ArrayList<>());
//
////            playerStands.forEach(Entity::remove);
//            if armorStands.stream().filter(a-> a.getLocation() == x.loc()).count() == 0 {
//                ArmorStand stand = (ArmorStand) world.spawnEntity(
//                    x.loc(), EntityType.ARMOR_STAND
//                );
//
//                stand.customName(Component.text(x.name()));
//                stand.setCustomNameVisible(true);
//                stand.setInvisible(true);
//                stand.setGravity(false);
//                armorStands.add(stand);
//
//            };
//
////            armourStands.put(player, playerStands);
//        });

    }
}
