package net.johnbrooks.ports.listeners;

import net.johnbrooks.ports.ports.Port;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.Optional;

public class PortUseEvents implements Listener {

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!event.isCancelled() && event.getTo() != null) {
            if (event.getFrom().getBlockX() != event.getTo().getBlockX() ||
                    event.getFrom().getBlockY() != event.getTo().getBlockY() ||
                    event.getFrom().getBlockZ() != event.getTo().getBlockZ()) {
                Optional<Port> optionalPort = Port.getPort(event.getTo());
                optionalPort.ifPresent(port -> port.useBridge(event.getPlayer()));
            }
        }
    }

}
