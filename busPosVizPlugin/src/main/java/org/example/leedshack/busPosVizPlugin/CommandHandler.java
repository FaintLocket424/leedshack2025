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
import java.util.ArrayList;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.scheduler.BukkitScheduler;
import org.example.leedshack.busPosVizPlugin.listeners.CustomPlayerMoveEvent;

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

                                                            handleStart(
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
                                                .executes(ctx ->
                                                    extracted(ctx,
                                                        -1.547507748221135,
                                                        53.80393562972413
                                                    )
                                                )
                                        )
                                )
                                .then(
                                    Commands.literal("durham")
                                        .then(
                                            Commands.argument("zoom",
                                                    DoubleArgumentType.doubleArg())
                                                .executes(ctx ->
                                                    extracted(ctx,
                                                        -1.5767460456129585,
                                                        54.77326250731719
                                                    )
                                                )
                                        )
                                )
                                .then(
                                    Commands.literal("london")
                                        .then(
                                            Commands.argument("zoom",
                                                    DoubleArgumentType.doubleArg())
                                                .executes(ctx ->
                                                    extracted(ctx,
                                                        -0.14231606305327568,
                                                        51.50121012110383
                                                    )
                                                )
                                        )
                                )
                                .then(
                                    Commands.literal("portsmouth")
                                        .then(
                                            Commands.argument("zoom",
                                                    DoubleArgumentType.doubleArg())
                                                .executes(ctx ->
                                                    extracted(ctx,
                                                        -1.1160587373098623,
                                                        50.82206947539475
                                                    )
                                                )
                                        )
                                )
                        )
                        .then(
                            Commands.literal("end")
                                .executes(ctx -> {
                                    CommandSender sender = ctx.getSource().getSender();

                                    World world = ctx.getSource().getLocation().getWorld();

                                    return handleEnd(world, sender);
                                })
                        )
                        .then(
                            Commands.literal("clear")
                                .executes(ctx -> {
                                    CommandSender sender = ctx.getSource().getSender();

                                    World world = ctx.getSource().getLocation().getWorld();

                                    return handleEnd(world, sender);
                                })
                        )
                        .then(
                            Commands.literal("view")
                                .executes(CommandHandler::viewArea)
                        )
                        .build();

                commands.registrar().register(busCommand);
            });
    }

    private static int extracted(CommandContext<CommandSourceStack> ctx, double longitude,
        double latitude) {
        double zoom = ctx.getArgument("zoom",
            double.class);

        World world = ctx.getSource().getLocation()
            .getWorld();
        CommandSender sender = ctx.getSource()
            .getSender();

        handleStart(
            new GlobalLocation(longitude,
                latitude, 0), zoom, world,
            sender);
        return Command.SINGLE_SUCCESS;
    }

    private static int viewArea(CommandContext<CommandSourceStack> ctx) {
        if (ctx.getSource().getSender() instanceof Player player) {
            player.teleport(new Location(player.getWorld(), 0, 110, 0, 180, 90));
            player.setFlying(true);
        }

        return Command.SINGLE_SUCCESS;
    }

    private static void handleStart(
        GlobalLocation centre,
        double zoom,
        World world,
        CommandSender sender
    ) {
        handleEnd(world, sender);

        BukkitScheduler scheduler = BusPosVizPlugin.instance.getServer().getScheduler();

        scheduler.runTaskAsynchronously(BusPosVizPlugin.instance, () -> {
            BoundingBox bb = Placeholder.getBoundingBox(centre, zoom);

            BusPosVizPlugin.BusStops = Placeholder.stopsWithinRegion(
                bb, BusPosVizPlugin.STOPS_FILENAME
            );
            BusPosVizPlugin.BusStopsMC = new ArrayList<>();
//            for (BusStop busStop : BusPosVizPlugin.BusStops) {
//                BusPosVizPlugin.BusStopsMC.add(new BusStopMC(scaledLocation, busStop.name()));
//            }

            scheduler.runTask(BusPosVizPlugin.instance, () -> {
                sender.sendPlainMessage(
                    String.format("Found %d bus stops", BusPosVizPlugin.BusStops.size()));
            });
            scheduler.runTaskTimer(BusPosVizPlugin.instance, () -> {
                for (BusStop busStop : BusPosVizPlugin.BusStops) {
                    var loc = busStop.globalLocation();

                    Location scaledLocation = worldToMC(zoom, loc, centre, world);

                    scaledLocation.setY(scaledLocation.getY() + 0.5);

//                    ArmorStand as = (ArmorStand) world.spawnEntity(
//                        scaledLocation, EntityType.ARMOR_STAND
//                    );
//
//                    as.customName(Component.text(busStop.name()));
//                    as.setCustomNameVisible(true);
//
//                    ItemStack hat = new ItemStack(Material.DIAMOND_HELMET);
//                    as.getEquipment().setHelmet(hat);

//                    scaledLocation.getBlock().setType(Material.RED_CONCRETE);

                    world.spawnParticle(Particle.DUST, scaledLocation, 1, 0, 0, 0, 1,
                        new DustOptions(Color.AQUA, 1), true);
                }
            }, 0, Tick.tick().fromDuration(Duration.ofSeconds(3)));

//            if (BusPosVizPlugin.SHEEP_TASK != null) {
//                BusPosVizPlugin.SHEEP_TASK.cancel();
//            }
//
//            BusPosVizPlugin.SHEEP_TASK = scheduler.runTaskTimerAsynchronously(
//                BusPosVizPlugin.instance, () -> {
//                    List<Bus> busList = GTFSReal.regionBuses(bb);
//
//                    BusPosVizPlugin.instance.getServer().getScheduler()
//                        .runTask(BusPosVizPlugin.instance, () -> {
//                            sender.sendPlainMessage(
//                                String.format("Found %d buses", busList.size()));
//
//                            for (Bus bus : busList) {
//                                if (!BusPosVizPlugin.SHEEP_PREV_LOCATIONS.containsKey(bus.id())) {
//                                    BusPosVizPlugin.SHEEP_PREV_LOCATIONS.put(bus.id(),
//                                        bus.globalLocation());
//                                    continue;
//                                }
//
//                                if (!hasBusMoved(bus) && !BusPosVizPlugin.SHEEPS.containsKey(
//                                    bus.id())) {
//                                    // Bus has not moved and has not previously moved
//                                    continue;
//                                }
//
//                                Location loc = worldToMC(zoom, bus.globalLocation(), centre, world);
//
//                                if (BusPosVizPlugin.SHEEPS.containsKey(bus.id())) {
//                                    // sheep already exists.
//                                    Sheep sheep = BusPosVizPlugin.SHEEPS.get(bus.id());
//                                    sheep.teleport(loc);
//                                } else {
//                                    // sheep is new
//                                    Sheep sheep = (Sheep) world.spawnEntity(loc, EntityType.SHEEP);
//                                    sheep.setAI(false);
//                                    sheep.setPersistent(true);
//                                    BusPosVizPlugin.SHEEPS.put(bus.id(), sheep);
//                                    sheep.setColor(DyeColor.RED);
//                                    sheep.customName(Component.text(bus.id()));
//                                    sheep.setCustomNameVisible(true);
//                                }
//                            }
//
//                            sender.sendPlainMessage(
//                                String.format("Mapped %d buses", BusPosVizPlugin.SHEEPS.size()));
//                        });
//
//                }, 0L, Tick.tick().fromDuration(Duration.ofSeconds(5L)));
        });
    }

    private static boolean hasBusMoved(Bus bus) {
        GlobalLocation currentLocation = bus.globalLocation();
        GlobalLocation prevLocation = BusPosVizPlugin.SHEEP_PREV_LOCATIONS.get(bus.id());

        double delta_long = Math.abs(currentLocation.longitude() - prevLocation.longitude());
        double delta_lat = Math.abs(currentLocation.latitude() - prevLocation.latitude());

        double e = 1e-5;

        return (delta_long >= e || delta_lat >= e);
    }

    public static Location worldToMC(
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

//        double map_size = BusPosVizPlugin.RADIUS;
//
//        double lon_rad = Math.toRadians(loc.longitude());
//        double lat_rad = Math.toRadians(loc.latitude());
//
//        double x =
//            map_size / 2 + map_size / (2 * span) * (lon_rad - Math.toRadians(centre.longitude()))
//                * Math.cos(Math.toRadians(centre.latitude()));
//        double y =
//            map_size / 2 + map_size / (2 * span) * Math.log(Math.tan(Math.PI / 4 + lat_rad / 2))
//                - map_size / (2 * span) * Math.log(
//                Math.tan(Math.PI / 4 + Math.toRadians(centre.latitude()) / 2));
//
//        return new Location(world, x, -60, y);
    }

    private static int handleEnd(World world, CommandSender sender) {
        sender.sendPlainMessage("Stopping buses.");

        if (BusPosVizPlugin.SHEEP_TASK != null) {
            BusPosVizPlugin.SHEEP_TASK.cancel();
            BusPosVizPlugin.SHEEP_TASK = null;
        }

        for (Entity entity : world.getEntities()) {
            if (!(entity instanceof ArmorStand)
                && !(entity instanceof Sheep)) {
                continue;
            }
            entity.remove();
        }

        BusPosVizPlugin.SHEEPS.clear();
        BusPosVizPlugin.SHEEP_PREV_LOCATIONS.clear();

        BusPosVizPlugin.BusStopsMC.clear();

        CustomPlayerMoveEvent.clearStops();

        return Command.SINGLE_SUCCESS;
    }
}
