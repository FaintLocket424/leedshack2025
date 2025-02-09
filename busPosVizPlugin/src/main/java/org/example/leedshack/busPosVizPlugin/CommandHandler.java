package org.example.leedshack.busPosVizPlugin;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
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
                        Commands.literal("spawnBus")
                            .executes(ctx -> {
                                if (!(ctx.getSource().getSender() instanceof Player player)) {
                                    ctx.getSource().getSender().sendMessage("You must be a player to use this command!");
                                    return 1;
                                }

                                World world = player.getWorld();

                                world.spawnEntity(player.getLocation(), EntityType.SHEEP);

                                return Command.SINGLE_SUCCESS;
                            })
                        )
                            .build();

                commands.registrar().register(busCommand);
            });
    }
}
