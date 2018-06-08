package com.plushnode.atlacoremobs.commands;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.function.Predicate;

// Selects how to execute a command when multiple plugins shared the same command alias.
public class CommandDemultiplexer implements Listener {
    // The shared string to find and demultiplex.
    private String sharedSignal;
    // This is the command that gets ran when the conditional is false.
    private String defaultOutput;
    // This is the command that gets ran when the conditional is true.
    private String conditionalOutput;
    // This determines whether or not the conditionalOutput is substituted.
    private Predicate<Player> conditional;

    public CommandDemultiplexer(String sharedSignal, String defaultOutput, String conditionalOutput, Predicate<Player> conditional) {
        this.sharedSignal = sharedSignal;
        this.defaultOutput = defaultOutput;
        this.conditionalOutput = conditionalOutput;
        this.conditional = conditional;
    }

    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        String msg = event.getMessage();

        if (!msg.startsWith("/" + sharedSignal + " ") && !msg.equalsIgnoreCase("/" + sharedSignal)) return;

        if (conditional.test(event.getPlayer())) {
            msg = msg.replace("/" + sharedSignal, "/" + conditionalOutput);
        } else {
            msg = msg.replace("/" + sharedSignal, "/" + defaultOutput);
        }

        event.setMessage(msg);
    }
}
