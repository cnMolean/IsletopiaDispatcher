package com.molean.isletopiabungeetweaker;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StpCommand implements CommandExecutor {
    public StpCommand() {
        Bukkit.getPluginCommand("stp").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1)
            return true;
        Player player = (Player) sender;
        if (!player.isOp())
            return true;
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF(args[0]);
        player.sendPluginMessage(IsletopiaBungeeTweaker.getPlugin(), "BungeeCord", out.toByteArray());
        return true;
    }
}
