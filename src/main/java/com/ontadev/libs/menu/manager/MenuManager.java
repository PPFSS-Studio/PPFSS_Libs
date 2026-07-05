// PPFSS_Libs Plugin
// Авторские права (c) 2026 OntaDev
// Лицензия: MIT

package com.ontadev.libs.menu.manager;

import com.ontadev.libs.menu.AbstractMenu;
import com.ontadev.libs.player.PlayerSnapshot;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings({"UnusedReturnValue", "unused"})
public interface MenuManager {

    /**
     * Registers a menu, wires it back to this manager, and pre-builds &
     * caches its static {@link ItemStack}s on the main thread.
     */
    <T extends AbstractMenu> void registerMenu(T menu);

    /** Returns a previously registered menu by its {@link AbstractMenu#id()}, or {@code null}. */
    AbstractMenu getMenu(String id);

    /**
     * Opens the given menu for a player, rendering static + dynamic items.
     *
     */
    CompletableFuture<Void> open(AbstractMenu abstractMenu, PlayerSnapshot snapshot);

    CompletableFuture<Void> open(AbstractMenu abstractMenu, Player player);

    /**
     * Opens a registered menu by id. Throws {@link IllegalArgumentException} if unknown.
     *
     */
    CompletableFuture<Void> open(String menuId, PlayerSnapshot snapshot);

    /**
     * Closes the player's currently open menu, if any.
     *
     */
    CompletableFuture<Void> close(PlayerSnapshot snapshot);

    /**
     * Returns the menu currently open for this player, if any.
     */
    CompletableFuture<Optional<AbstractMenu>> activeMenu(PlayerSnapshot snapshot);

    /**
     * Re-resolves {@link AbstractMenu#dynamicItems(PlayerSnapshot)} for the player's
     * currently open menu and re-renders it in place (e.g. after a balance
     * change). No-op if the player has no menu open.
     *
     */
    CompletableFuture<Void> refresh(PlayerSnapshot snapshot);
}