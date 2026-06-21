// PPFSS_Libs Plugin
// Авторские права (c) 2026 OntaDev
// Лицензия: MIT

package com.ontadev.libs.menu;

import com.ontadev.libs.item.ItemModel;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.Inventory;

import java.util.Map;

/**
 * Per-player, per-open-instance state for a {@link AbstractMenu}. Created in
 * {@link MenuManagerImpl#open} and discarded once the inventory closes.
 */
@Getter
final class MenuSession {

    private final AbstractMenu abstractMenu;
    private final Inventory inventory;

    /** The currently rendered "slot -> ItemModel" map; replaced on each {@link MenuManagerImpl#refresh}. */
    @Setter
    private Map<Integer, ItemModel> items;

    MenuSession(AbstractMenu abstractMenu, Inventory inventory, Map<Integer, ItemModel> items) {
        this.abstractMenu = abstractMenu;
        this.inventory = inventory;
        this.items = items;
    }

    ItemModel itemAt(int rawSlot) {
        return items.get(rawSlot);
    }
}