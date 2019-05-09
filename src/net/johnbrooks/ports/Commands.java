package net.johnbrooks.ports;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import net.johnbrooks.ports.ports.Port;
import net.johnbrooks.ports.settings.Settings;
import net.johnbrooks.ports.settings.UpdateManager;
import net.johnbrooks.ports.utils.WorldEditUtil;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Commands implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (sender instanceof Player) {
                sender.sendMessage(ChatColor.BLUE + "[Ports] This plugin was created by WiseHollow!");
                Player player = (Player) sender;
                TextComponent message = new TextComponent(ChatColor.BLUE + "" + ChatColor.UNDERLINE + "Click here " +
                        ChatColor.RESET + "" + ChatColor.BLUE + "to see my profile and my other plugins! " + Main.plugin.getName() + "!");
                message.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.spigotmc.org/members/wisehollow.14804/"));
                player.spigot().sendMessage(message);
            } else
                sender.sendMessage(ChatColor.BLUE + Main.plugin.getName() + " was created by WiseHollow. Check out my other plugins on my SpigotMC profile!");
            return true;
        } else {
            if (args.length == 1) {
                if (args[0].equalsIgnoreCase("Version")) {
                    sender.sendMessage(ChatColor.BLUE + "[Ports] Current version is '" + Main.plugin.getDescription().getVersion() + "'.");
                    if (UpdateManager.isUpdateAvailable())
                        sender.sendMessage(ChatColor.BLUE + "[Ports] Update is available.");
                    else
                        sender.sendMessage(ChatColor.BLUE + "[Ports] Everything is up-to-date.");
                    return true;
                } else if (args[0].equalsIgnoreCase("Update")) {
                    if (UpdateManager.isUpdateAvailable()) {
                        if (sender instanceof Player) {
                            Player player = (Player) sender;
                            TextComponent message = new TextComponent(ChatColor.BLUE + "" + ChatColor.UNDERLINE + "Click here " +
                                    ChatColor.RESET + "" + ChatColor.BLUE + "to get the latest version of " + Main.plugin.getName() + "!");
                            message.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://goo.gl/aLYP5E"));
                            player.spigot().sendMessage(message);
                        } else
                            sender.sendMessage("Go to https://goo.gl/aLYP5E to get the latest version of " + Main.plugin.getName() + "!");
                    } else
                        sender.sendMessage(ChatColor.BLUE + "[Ports] Everything is up-to-date.");
                    return true;
                } else if (args[0].equalsIgnoreCase("reload")) {
                    Main.plugin.reloadConfig();
                    Settings.load();
                    sender.sendMessage(ChatColor.BLUE + "[Ports] Configuration has been reloaded!");
                    return true;
                } else if (args[0].equalsIgnoreCase("list")) {
                    sender.sendMessage(ChatColor.BLUE + "List of Ports: ");
                    for (Port port : Port.getAllPorts()) {
                        sender.sendMessage("Name: " + port.getName() + " | World: " + port.getWorld() + " (PointA: " + port.getPointA().getX() + "," + port.getPointA().getY() + "," + port.getPointA().getZ() + ")");
                    }
                    return true;
                } else if (args[0].equalsIgnoreCase("help")) {
                    sender.sendMessage("/port reload - Reload the config file.");
                    sender.sendMessage("/port create [name] - Renames the port.");
                    sender.sendMessage("/port delete [name] - Deletes the port.");
                    sender.sendMessage("/port arrive [name] - Sets the arrival location.");
                    sender.sendMessage("/port update [name] - Sets the activation zone. ");
                    sender.sendMessage("/port schedule [name] [schedule] - Sets the schedule.");
                    sender.sendMessage("/port describe [name] [port description] - Sets the description that the port will be described as.");
                    sender.sendMessage("/port destination [from] [to] - Sets [from]'s destination without editing [to]'s destination.");
                    sender.sendMessage("/port hardlink [from] [to] - Links the destination of [from] and [to] and making nothing else link to them.");
                    sender.sendMessage("/port link [from] [to] - Links the destination of [from] and [to].");
                    sender.sendMessage("/port list - Lists all ports.");
                    sender.sendMessage("/port rename [old-name] [new-name] - Renames the port.");
                    sender.sendMessage("/port permission [port] [permission.node] - Sets the required permission for Port use. ");
                    return true;
                }
            } else if (args.length == 2) {
                if (args[0].equalsIgnoreCase("Create")) {
                    if (!(sender instanceof Player)) {
                        sender.sendMessage("[Ports] You must be logged in to do this!");
                        return true;
                    }
                    Player player = (Player) sender;
                    Region selection = WorldEditUtil.getSelection(player);

                    if (selection != null) {
                        String portName = args[1];
                        Location createLocation = player.getLocation();
                        BlockVector3 minimumPoint = selection.getMinimumPoint();
                        BlockVector3 maximumPoint = selection.getMaximumPoint();
                        Port port = Port.insertNewPort(portName, player.getLocation().getWorld().getName(),
                                minimumPoint, maximumPoint, createLocation);

                        if (port != null) {
                            Main.logger.info("Port was created by: " + player.getName());
                            player.sendMessage(ChatColor.BLUE + "[Ports] The port was successfully created.");
                        } else {
                            player.sendMessage(ChatColor.BLUE + "[Ports] Could not create port!");
                        }
                    } else {
                        player.sendMessage(ChatColor.BLUE + "[Ports] You've not made a selection yet.");
                    }

                    return true;
                } else if (args[0].equalsIgnoreCase("Delete") || args[0].equalsIgnoreCase("Remove")) {
                    String portName = args[1];
                    Port port = Port.getPort(portName);
                    if (port != null) {
                        port.delete();
                        sender.sendMessage(ChatColor.BLUE + "[Ports] The selected port has been deleted!");
                    } else {
                        sender.sendMessage(ChatColor.BLUE + "[Ports] That port does not exist.");
                    }

                    return true;
                } else if (args[0].equalsIgnoreCase("Update")) {
                    // Set ports regions to worldedit selection.
                    if (!(sender instanceof Player)) {
                        sender.sendMessage("[Ports] You must be logged in to do this!");
                        return true;
                    }
                    Player player = (Player) sender;
                    Region selection = WorldEditUtil.getSelection(player);

                    if (selection != null) {
                        String portName = args[1];
                        Port port = Port.getPort(portName);
                        if (port != null) {
                            Location locationA = new Location(player.getWorld(), selection.getMinimumPoint().getBlockX(), selection.getMinimumPoint().getBlockY(), selection.getMinimumPoint().getBlockZ());
                            Location locationB = new Location(player.getWorld(), selection.getMaximumPoint().getBlockX(), selection.getMaximumPoint().getBlockY(), selection.getMaximumPoint().getBlockZ());
                            port.setPointA(locationA);
                            port.setPointB(locationB);
                            try {
                                port.save();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            player.sendMessage(ChatColor.BLUE + "[Ports] The port was successfully updated.");
                        } else {
                            player.sendMessage(ChatColor.BLUE + "[Ports] Specified port does not exist!");
                        }
                    } else {
                        player.sendMessage(ChatColor.BLUE + "[Ports] Invalid type of selection!");
                    }

                    return true;
                } else if (args[0].equalsIgnoreCase("Arrive")) {
                    // Set arrival location
                    if (!(sender instanceof Player)) {
                        sender.sendMessage("[Ports] You must be logged in to do this!");
                        return true;
                    }
                    Player player = (Player) sender;
                    String portName = args[1];
                    Port port = Port.getPort(portName);
                    if (port != null) {
                        port.setCreateLocation(player.getLocation());
                        try {
                            port.save();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        player.sendMessage(ChatColor.BLUE + "[Ports] The port's arrival was successfully updated.");
                    } else {
                        player.sendMessage(ChatColor.BLUE + "[Ports] Specified port does not exist!");
                    }

                    return true;
                } else if (args[0].equalsIgnoreCase("Permission")) {
                    Port port = Port.getPort(args[1]);
                    if (port == null) {
                        sender.sendMessage(ChatColor.BLUE + "[Ports] The port does not exist.");
                        return true;
                    }
                    port.setPermission(null);
                    try {
                        port.save();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    sender.sendMessage(ChatColor.BLUE + "[Ports] " + args[1] + "'s permission node has been removed.");

                    return true;
                }
            } else if (args.length == 3) {
                if (args[0].equalsIgnoreCase("HardLink")) {
                    Port portA = Port.getPort(args[1]);
                    Port portB = Port.getPort(args[2]);

                    if (portA != null && portB != null) {
                        if (portA.getConnection() != null && portA.getConnection() != portB)
                            portA.getConnection().setConnection(null);
                        if (portB.getConnection() != null && portB.getConnection() != portA)
                            portB.getConnection().setConnection(null);

                        portA.setConnection(portB);
                        portB.setConnection(portA);
                        sender.sendMessage(ChatColor.BLUE + "[Ports] Destinations updated for '" + portA.getName() + "' and '" + portB.getName() + "'.");
                        try {
                            portA.save();
                            portB.save();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        sender.sendMessage(ChatColor.BLUE + "[Ports] One of these ports do not exist.");
                    }
                }
                if (args[0].equalsIgnoreCase("Link")) {
                    Port portA = Port.getPort(args[1]);
                    Port portB = Port.getPort(args[2]);

                    if (portA != null && portB != null) {
                        portA.setConnection(portB);
                        portB.setConnection(portA);
                        sender.sendMessage(ChatColor.BLUE + "[Ports] Destinations updated for '" + portA.getName() + "' and '" + portB.getName() + "'.");
                        try {
                            portA.save();
                            portB.save();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        sender.sendMessage(ChatColor.BLUE + "[Ports] One of these ports do not exist.");
                    }
                } else if (args[0].equalsIgnoreCase("Destination")) {
                    Port portA = Port.getPort(args[1]);
                    Port portB = Port.getPort(args[2]);

                    if (portA != null && portB != null) {
                        portA.setConnection(portB);
                        sender.sendMessage(ChatColor.BLUE + "[Ports] Destinations updated for '" + portA.getName() + "'.");
                        try {
                            portA.save();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        sender.sendMessage(ChatColor.BLUE + "[Ports] One of these ports do not exist.");
                    }
                } else if (args[0].equalsIgnoreCase("Schedule")) {
                    Port port = Port.getPort(args[1]);
                    if (port == null) {
                        sender.sendMessage(ChatColor.BLUE + "[Ports] The port does not exist.");
                        return true;
                    }
                    int departure;
                    try {
                        departure = Integer.parseInt(args[2]);
                    } catch (NumberFormatException ex) {
                        sender.sendMessage(ChatColor.BLUE + "[Ports] " + args[2] + " is not a number.");
                        return true;
                    }
                    port.setDeparture(departure);
                    try {
                        port.save();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    sender.sendMessage(ChatColor.BLUE + "[Ports] " + args[1] + "'s departure time is now " + departure + " minute.");
                } else if (args[0].equalsIgnoreCase("Permission")) {
                    Port port = Port.getPort(args[1]);
                    if (port == null) {
                        sender.sendMessage(ChatColor.BLUE + "[Ports] The port does not exist.");
                        return true;
                    }
                    String permission = args[2];
                    port.setPermission(permission);
                    try {
                        port.save();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    sender.sendMessage(ChatColor.BLUE + "[Ports] " + args[1] + "'s permission node is now '" + permission + "'.");
                } else if (args[0].equalsIgnoreCase("Rename")) {
                    Port port = Port.getPort(args[1]);
                    if (port == null) {
                        sender.sendMessage(ChatColor.BLUE + "[Ports] The port does not exist.");
                        return true;
                    }
                    File file = new File("plugins" + File.separator + Main.plugin.getName() + File.separator + "ports" + File.separator + port.getFileName());
                    file.delete();

                    String nameToSet = args[2];
                    port.setName(nameToSet);
                    try {
                        port.save();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    sender.sendMessage(ChatColor.BLUE + "[Ports] " + args[1] + "'s name is now '" + nameToSet + "'.");
                }

                return true;
            } else {
                if (args[0].equalsIgnoreCase("Describe")) {
                    Port port = Port.getPort(args[1]);
                    if (port == null) {
                        sender.sendMessage(ChatColor.BLUE + "[Ports] The port does not exist.");
                        return true;
                    }
                    String description = "";
                    for (int i = 2; i < args.length; i++)
                        description += args[i] + " ";
                    description = description.substring(0, description.length() - 1);
                    port.setDescription(description);
                    try {
                        port.save();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    sender.sendMessage(ChatColor.BLUE + "[Ports] " + args[1] + "'s description node is now '" + description + "'.");
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {
        String[] tabs;

        int argLength = args.length;
        switch(argLength) {
            case 1:
                tabs = new String[] { "version", "reload", "list", "create", "update", "delete", "remove", "arrive", "hardlink", "link", "destination", "schedule", "permission", "rename", "describe" };
                break;
            case 2:
                tabs = new String[] { "<PORT_NAME>" };
                break;
            case 3:
                tabs = new String[] { "<PORT_NAME>" };
                break;
            default:
                tabs = new String[] {};
                break;
        }

        return Arrays.stream(tabs).collect(Collectors.toList());
    }
}
