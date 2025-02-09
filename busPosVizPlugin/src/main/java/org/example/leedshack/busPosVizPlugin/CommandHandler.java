package org.example.leedshack.busPosVizPlugin;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import java.util.List;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

@SuppressWarnings("UnstableApiUsage")
public class CommandHandler {

    public static void registerCommands() {
        BusPosVizPlugin.instance.getLifecycleManager()
            .registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
                LiteralCommandNode<CommandSourceStack> busCommand =
                    Commands.literal("bus")
                        .then(
                            Commands.literal("stops")
                                .then(
                                    Commands.literal("place")
                                        .executes(CommandHandler::placeBusStops)
                                )
                        )
                        .then(
                            Commands.literal("run") // /bus run
                        )
                        .build();

                commands.registrar().register(busCommand);
            });
    }

    private static int placeBusStops(CommandContext<CommandSourceStack> ctx) {
        var centre = new GlobalLocation(53.7996, -1.5471, 0);
        var span = 0.05;

        BoundingBox bb = Placeholder.getBoundingBox(centre, span);

        List<BusStop> busStops = Placeholder.stopsWithinRegion(bb, BusPosVizPlugin.STOPS_FILENAME);

        for (BusStop busStop : busStops) {
            GlobalLocation gLoc = busStop.globalLocation();
            ctx.getSource().getSender().sendPlainMessage(String.format("%f long; %f lat; %s name", gLoc.longitude(), gLoc.latitude(), busStop.name()));
        }

        return Command.SINGLE_SUCCESS;
    }
}
