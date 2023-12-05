package me.colormaestro.taskmanager.listeners.inventory;

import me.colormaestro.taskmanager.utils.DataContainerKeys;
import me.colormaestro.taskmanager.utils.RunnablesCreator;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
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
            handleEvent(event.getWhoClicked(), event.getCurrentItem(), event.getClick());
        }
    }

    /**
     * Called when directive in event view's matches listeners directive.
     *
     * @param player    which invoked the event
     * @param itemStack which was clicked by player
     * @param clickType on player's mouse
     */
    abstract void handleEvent(HumanEntity player, ItemStack itemStack, ClickType clickType);

    /**
     * Extracts value from persistent data container of holder.
     *
     * @param holder from which to extract value
     * @param key    under which is the value stored
     * @param type   of value
     * @return value or null if nothing is stored under the key
     */
    <T, Z> Z extractPersistentValue(PersistentDataHolder holder, String key, PersistentDataType<T, Z> type) {
        return holder.getPersistentDataContainer().get(new NamespacedKey(creator.getPlugin(), key), type);
    }

    /**
     * Checks whether persistent data container contains instruction for next page in pagination process.
     *
     * @param holder in which to check for instruction
     * @return true if instruction is present, false otherwise
     */
    boolean isPaginationForward(PersistentDataHolder holder) {
        return holder.getPersistentDataContainer().has(
                new NamespacedKey(creator.getPlugin(), DataContainerKeys.TURN_NEXT_PAGE), PersistentDataType.STRING);
    }

    /**
     * Determines which page should be displayed according to data in provided persistent holder.
     *
     * @param holder from which to determine next page
     * @return number of page which should be displayed next
     */
    int determineNextPage(PersistentDataHolder holder) {
        int currentPage = extractPersistentValue(holder, DataContainerKeys.CURRENT_PAGE, PersistentDataType.INTEGER);
        int totalPages = extractPersistentValue(holder, DataContainerKeys.TOTAL_PAGES, PersistentDataType.INTEGER);

        if (isPaginationForward(holder)) {
            currentPage++;
        } else {
            currentPage--;
        }

        if (currentPage > totalPages) {
            currentPage = 1;
        } else if (currentPage < 1) {
            currentPage = totalPages;
        }

        return currentPage;
    }
}
