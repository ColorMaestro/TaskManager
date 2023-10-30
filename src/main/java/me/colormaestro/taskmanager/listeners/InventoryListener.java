package me.colormaestro.taskmanager.listeners;

import me.colormaestro.taskmanager.utils.RunnablesCreator;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.lang.annotation.Inherited;

public abstract class InventoryListener implements Listener {
    protected final RunnablesCreator creator;
    private final String directive;

    InventoryListener(RunnablesCreator creator, String directive) {
        this.creator = creator;
        this.directive = directive;
    }

    @EventHandler
    void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().contains(directive) && event.getCurrentItem() != null) {
            event.setCancelled(true);
            handleEvent(event);
        }
    }

    /**
     * Called when directive in event view's matches listeners directive.
     *
     * @param event captured by handler method
     */
    abstract void handleEvent(InventoryClickEvent event);
}
