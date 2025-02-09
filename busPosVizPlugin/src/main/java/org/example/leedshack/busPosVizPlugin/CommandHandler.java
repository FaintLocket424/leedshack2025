package org.example.leedshack.busPosVizPlugin;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import io.papermc.paper.util.Tick;
import java.time.Duration;
import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitScheduler;

@SuppressWarnings("UnstableApiUsage")
public class CommandHandler {

    public static void registerCommands() {
        /*
        /bus start <location>
        /bus start <lat> <long> <zoom>

        Command to show the bus stations and start the bus position system.

        ==================================

        /bus end

        Command to clean up the armour stands + sheep, and to stop the sheep polling.
         */

        // Command to enable the
        // /bus start
        // /bus end

        BusPosVizPlugin.instance.getLifecycleManager()
            .registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
                LiteralCommandNode<CommandSourceStack> busCommand =
                    Commands.literal("bus")
                        .then(
                            Commands.literal("start")
                                .then(
                                    Commands.argument("latitude", DoubleArgumentType.doubleArg())
                                        .then(
                                            Commands.argument("longitude",
                                                    DoubleArgumentType.doubleArg())
                                                .then(
                                                    Commands.argument("zoom",
                                                            DoubleArgumentType.doubleArg())
                                                        .executes(ctx -> {
                                                            // /bus start <latitude> <longitude> <zoom>
                                                            double latitude = ctx.getArgument(
                                                                "latitude", double.class);
                                                            double longitude = ctx.getArgument(
                                                                "longitude", double.class);
                                                            double zoom = ctx.getArgument("zoom",
                                                                double.class);

                                                            var centre = new GlobalLocation(
                                                                longitude, latitude, 0.0);

                                                            handleCommand(
                                                                centre,
                                                                zoom,
                                                                ctx.getSource().getLocation()
                                                                    .getWorld(),
                                                                ctx.getSource().getSender());

                                                            return Command.SINGLE_SUCCESS;
                                                        })
                                                )
                                        )
                                )
                                .then(
                                    Commands.literal("leeds")
                                        .then(
                                            Commands.argument("zoom",
                                                    DoubleArgumentType.doubleArg())
                                                .executes(ctx -> {
                                                        double zoom = ctx.getArgument("zoom",
                                                            double.class);

                                                        World world = ctx.getSource().getLocation()
                                                            .getWorld();
                                                        CommandSender sender = ctx.getSource()
                                                            .getSender();

                                                        handleCommand(
                                                            new GlobalLocation(53.80393562972413,
                                                                -1.547507748221135, 0), zoom, world,
                                                            sender);
                                                        return Command.SINGLE_SUCCESS;
                                                    }
                                                )
                                        )
                                )
                                .then(
                                    Commands.literal("durham")
                                        .then(
                                            Commands.argument("zoom",
                                                DoubleArgumentType.doubleArg())
                                        )
                                )
                                .then(
                                    Commands.literal("london")
                                        .then(
                                            Commands.argument("zoom",
                                                DoubleArgumentType.doubleArg())
                                        )
                                )
                                .then(
                                    Commands.literal("portsmouth")
                                        .then(
                                            Commands.argument("zoom",
                                                DoubleArgumentType.doubleArg())
                                        )
                                )
                        )
                        .then(
                            Commands.literal("end")
                                .executes(CommandHandler::handleEnd)
                        )
                        .then(
                            Commands.literal("view")
                                .executes(CommandHandler::viewArea)
                        )
                        .build();

                commands.registrar().register(busCommand);
            });
    }

    private static int viewArea(CommandContext<CommandSourceStack> ctx) {
        if (ctx.getSource().getSender() instanceof Player player) {
            player.teleport(new Location(player.getWorld(), 0, 20, 0, 180, 90));
            player.setFlying(true);
        }

        return Command.SINGLE_SUCCESS;
    }

    private static void handleCommand(
        GlobalLocation centre,
        double zoom,
        World world,
        CommandSender sender
    ) {
        BukkitScheduler scheduler = BusPosVizPlugin.instance.getServer().getScheduler();

        scheduler.runTaskAsynchronously(BusPosVizPlugin.instance, () -> {
            BoundingBox bb = Placeholder.getBoundingBox(centre, zoom);

            List<BusStop> busStops = Placeholder.stopsWithinRegion(
                bb, BusPosVizPlugin.STOPS_FILENAME
            );

            scheduler.runTask(BusPosVizPlugin.instance, () -> {
                sender.sendPlainMessage(
                    String.format("Found %d bus stops", busStops.size()));

                for (BusStop busStop : busStops) {
                    var loc = busStop.globalLocation();

                    Location scaledLocation = worldToMC(zoom, loc, centre, world);

                    ArmorStand as = (ArmorStand) world.spawnEntity(
                        scaledLocation, EntityType.ARMOR_STAND
                    );

                    as.customName(Component.text(busStop.name()));
                    as.setCustomNameVisible(true);

                    ItemStack hat = new ItemStack(Material.DIAMOND_HELMET);
                    as.getEquipment().setHelmet(hat);
                }
            });

            if (BusPosVizPlugin.SHEEP_TASK != null) {
                BusPosVizPlugin.SHEEP_TASK.cancel();
            }

            BusPosVizPlugin.SHEEP_TASK = scheduler.runTaskTimerAsynchronously(
                BusPosVizPlugin.instance, () -> {
                    List<Bus> busList = GTFSReal.regionBuses(bb);

                    BusPosVizPlugin.instance.getServer().getScheduler()
                        .runTask(BusPosVizPlugin.instance, () -> {
                            sender.sendPlainMessage(
                                String.format("Found %d buses", busList.size()));

                            for (Bus bus : busList) {
                                if (!BusPosVizPlugin.SHEEP_PREV_LOCATIONS.containsKey(bus.id())) {
                                    BusPosVizPlugin.SHEEP_PREV_LOCATIONS.put(bus.id(),
                                        bus.globalLocation());
                                    continue;
                                }

                                if (!hasBusMoved(bus) && !BusPosVizPlugin.SHEEPS.containsKey(
                                    bus.id())) {
                                    // Bus has not moved and has not previously moved
                                    continue;
                                }

                                Location loc = worldToMC(zoom, bus.globalLocation(), centre, world);

                                if (BusPosVizPlugin.SHEEPS.containsKey(bus.id())) {
                                    // sheep already exists.
                                    Sheep sheep = BusPosVizPlugin.SHEEPS.get(bus.id());
                                    sheep.teleport(loc);
                                } else {
                                    // sheep is new
                                    Sheep sheep = (Sheep) world.spawnEntity(loc, EntityType.SHEEP);
                                    sheep.setAI(false);
                                    sheep.setPersistent(true);
                                    BusPosVizPlugin.SHEEPS.put(bus.id(), sheep);
                                    sheep.setColor(DyeColor.RED);
                                }
                            }

                            sender.sendPlainMessage(
                                String.format("Mapped %d buses", BusPosVizPlugin.SHEEPS.size()));
                        });

                }, 0L, Tick.tick().fromDuration(Duration.ofSeconds(5L)));
        });
    }

//    private static int placeBusStops(CommandContext<CommandSourceStack> ctx) {
//        double span = ctx.getArgument("span", double.class);
//        double longitude = ctx.getArgument("longitude", double.class);
//        double latitude = ctx.getArgument("latitude", double.class);
//        var centre = new GlobalLocation(longitude, latitude, 0);
//
//        BusPosVizPlugin.instance.getServer().getScheduler()
//            .runTaskAsynchronously(BusPosVizPlugin.instance, () -> {
//                BoundingBox bb = Placeholder.getBoundingBox(centre, span);
//
//                List<BusStop> busStops = Placeholder.stopsWithinRegion(bb,
//                    BusPosVizPlugin.STOPS_FILENAME);
//
//                BusPosVizPlugin.instance.getServer().getScheduler()
//                    .runTask(BusPosVizPlugin.instance, () -> {
//                        ctx.getSource().getSender()
//                            .sendPlainMessage(String.format("Found %d bus stops", busStops.size()));
//
//                        for (BusStop busStop : busStops) {
//                            var loc = busStop.globalLocation();
//
//                            World world = ctx.getSource().getSender().getServer().getWorlds()
//                                .get(0);
//
//                            Location scaledLocation = worldToMC(span, loc, centre, world);
//
//                            ArmorStand as = (ArmorStand) world.spawnEntity(scaledLocation,
//                                EntityType.ARMOR_STAND);
//
//                            as.customName(Component.text(busStop.name()));
//                            as.setCustomNameVisible(true);
//
//                            ItemStack hat = new ItemStack(Material.DIAMOND_HELMET);
//
//                            as.getEquipment().setItem(EquipmentSlot.HEAD, hat);
//                        }
//                    });
//            });
//
//        return Command.SINGLE_SUCCESS;
//    }

//    private static int spawnSheep(CommandContext<CommandSourceStack> ctx) {
//        double span = ctx.getArgument("span", double.class);
//        double longitude = ctx.getArgument("longitude", double.class);
//        double latitude = ctx.getArgument("latitude", double.class);
//        var centre = new GlobalLocation(longitude, latitude, 0);
//        BoundingBox bb = Placeholder.getBoundingBox(centre, span);
//
//        if (BusPosVizPlugin.SHEEP_TASK != null) {
//            BusPosVizPlugin.SHEEP_TASK.cancel();
//        }
//
//        BusPosVizPlugin.SHEEP_TASK = BusPosVizPlugin.instance.getServer().getScheduler()
//            .runTaskTimerAsynchronously(BusPosVizPlugin.instance, () -> {
//                List<Bus> busList = GTFSReal.regionBuses(bb);
//
//                BusPosVizPlugin.instance.getServer().getScheduler()
//                    .runTask(BusPosVizPlugin.instance, () -> {
//                        CommandSender sender = ctx.getSource().getSender();
//                        sender.sendPlainMessage(String.format("Found %d buses", busList.size()));
//
//                        World world = BusPosVizPlugin.instance.getServer().getWorlds().get(0);
//                        for (Bus bus : busList) {
//                            if (!BusPosVizPlugin.SHEEP_PREV_LOCATIONS.containsKey(bus.id())) {
//                                BusPosVizPlugin.SHEEP_PREV_LOCATIONS.put(bus.id(),
//                                    bus.globalLocation());
//                                continue;
//                            }
//
//                            if (!hasBusMoved(bus) && !BusPosVizPlugin.SHEEPS.containsKey(
//                                bus.id())) {
//                                // Bus has not moved and has not previously moved
//                                continue;
//                            }
//
//                            Location loc = worldToMC(span, bus.globalLocation(), centre, world);
//
//                            if (BusPosVizPlugin.SHEEPS.containsKey(bus.id())) {
//                                // sheep already exists.
//                                Sheep sheep = BusPosVizPlugin.SHEEPS.get(bus.id());
//                                sheep.teleport(loc);
//                            } else {
//                                // sheep is new
//                                Sheep sheep = (Sheep) world.spawnEntity(loc, EntityType.SHEEP);
//                                sheep.setAI(false);
//                                sheep.setPersistent(true);
//                                BusPosVizPlugin.SHEEPS.put(bus.id(), sheep);
//                                sheep.setColor(DyeColor.RED);
//                            }
//                        }
//
//                        sender.sendPlainMessage(
//                            String.format("Mapped %d buses", BusPosVizPlugin.SHEEPS.size()));
//                    });
//
//            }, 0L, Tick.tick().fromDuration(Duration.ofSeconds(10L)));
//
//        return Command.SINGLE_SUCCESS;
//    }

    private static boolean hasBusMoved(Bus bus) {
        GlobalLocation currentLocation = bus.globalLocation();
        GlobalLocation prevLocation = BusPosVizPlugin.SHEEP_PREV_LOCATIONS.get(bus.id());

        double delta_long = Math.abs(currentLocation.longitude() - prevLocation.longitude());
        double delta_lat = Math.abs(currentLocation.latitude() - prevLocation.latitude());

        double e = 1e-5;

        return (delta_long >= e || delta_lat >= e);
    }

    private static Location worldToMC(
        double span,
        GlobalLocation loc,
        GlobalLocation centre,
        World world
    ) {
        var transformed_location = new GlobalLocation(
            BusPosVizPlugin.RADIUS / span * (loc.longitude() - centre.longitude()),
            BusPosVizPlugin.RADIUS / span * (loc.latitude() - centre.latitude()),
            loc.bearing()
        );

        return new Location(world,
            transformed_location.longitude(),
            -60,
            -transformed_location.latitude(),
            (float) transformed_location.bearing(),
            0
        );
    }

    private static int handleEnd(CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();

        World world = ctx.getSource().getLocation().getWorld();

        sender.sendPlainMessage("Stopping buses.");

        if (BusPosVizPlugin.SHEEP_TASK != null) {
            BusPosVizPlugin.SHEEP_TASK.cancel();
        }

        for (Entity entity : world.getEntities()) {
            if (!(entity instanceof ArmorStand)
                && !(entity instanceof Sheep)) {
                continue;
            }
            entity.remove();
        }

        return Command.SINGLE_SUCCESS;
    }
}
