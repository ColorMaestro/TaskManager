package me.colormaestro.taskmanager.listeners;

import me.colormaestro.taskmanager.utils.RunnablesCreator;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;

public abstract class InventoryListener implements Listener {
    protected final RunnablesCreator creator;
    private final String directive;

    InventoryListener(RunnablesCreator creator, String directive) {
        this.creator = creator;
        this.directive = directive;
    }

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

    /**
     * Extracts value from persistent data container of holder.
     *
     * @param holder from which to extract value
     * @param key under which is the value stored
     * @param type of value
     * @return value or null if nothing is stored under the key
     */
    <T, Z> Z extractPersistentValue(PersistentDataHolder holder, String key, PersistentDataType<T, Z> type) {
        return holder.getPersistentDataContainer().get(new NamespacedKey(creator.getPlugin(), key), type);
    }

    /**
     * Checks whether persistent data container of holder contains value.
     *
     * @param holder in which to check value
     * @param key under which is the value stored
     * @param type of value
     * @return true if a value is present in storage for given key, false otherwise
     */
    <T, Z> boolean hasPersistentValue(PersistentDataHolder holder, String key, PersistentDataType<T, Z> type) {
        return holder.getPersistentDataContainer().has(new NamespacedKey(creator.getPlugin(), key), type);
    }
}
