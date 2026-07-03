// OntaDev_Libs Plugin
// Авторские права (c) 2025 OntaDev
// Лицензия: MIT

package com.ontadev.libs.message;

import lombok.Getter;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Не потокобезопасен для конкурентной мутации (add/clear) во время send.
 * Предполагается паттерн использования: собрали сообщение -> разослали.
 */
@Getter
@SuppressWarnings("unused")
public class Message {

    private static Plugin staticPlugin;
    private static final MiniMessage MINI_MESSAGE = MiniMessage.builder().strict(false).build();
    private static final PlainTextComponentSerializer PLAIN = PlainTextComponentSerializer.plainText();
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacyAmpersand();
    private static final Pattern LEGACY_TAG_PATTERN = Pattern.compile("</?&([\\p{L}\\p{Nd}_]+)>", Pattern.CASE_INSENSITIVE);

    public static void load(Plugin plugin) {
        Message.staticPlugin = plugin;
    }

    private final Plugin plugin;
    private final List<String> rawMessage = new ArrayList<>();


    private volatile List<Component> parsedCache;

    public Message() {
        this.plugin = staticPlugin;
    }

    public Message(@NotNull String... messages) {
        this();
        Collections.addAll(rawMessage, messages);
    }

    public Message(@NotNull List<String> messages) {
        this();
        rawMessage.addAll(messages);
    }

    public Message(@NotNull Component... components) {
        this();
        for (Component component : components) {
            rawMessage.add(PLAIN.serialize(component));
        }
    }

    public Message(@NotNull Plugin plugin, @NotNull String... messages) {
        this.plugin = plugin;
        Collections.addAll(rawMessage, messages);
    }

    public void add(@NotNull String message) {
        rawMessage.add(message);
        invalidateCache();
    }

    public void addAll(@NotNull List<String> messages) {
        rawMessage.addAll(messages);
        invalidateCache();
    }

    public void add(@NotNull Component component) {
        rawMessage.add(PLAIN.serialize(component));
        invalidateCache();
    }

    public void clear() {
        rawMessage.clear();
        invalidateCache();
    }

    private void invalidateCache() {
        parsedCache = null;
    }

    // 
    // Отправка сообщений
    // 

    public void send(UUID uuid) {
        runOnMain(() -> {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) sendSync(player);
        });
    }

    public void send(Player audience) {
        if (audience == null) return;
        runOnMain(() -> sendSync(audience));
    }

    public void send(CommandSender sender) {
        if (sender == null) return;
        if (sender instanceof Player) {
            Player player = (Player) sender;
            send(player);
            return;
        }
        runOnMain(() -> sendSync(sender));
    }

    public void send(Player player, @NotNull Placeholders placeholders) {
        if (player == null) return;
        runOnMain(() -> sendSync(player, placeholders));
    }

    public void send(CommandSender sender, @NotNull Placeholders placeholders) {
        if (sender == null) return;
        if (sender instanceof Player) {
            Player player = (Player) sender;
            send(player, placeholders);
            return;
        }
        runOnMain(() -> sendSync(sender, placeholders));
    }

    public void sendActionBar(Audience audience) {
        if (audience == null || rawMessage.isEmpty()) return;
        runOnMain(() -> audience.sendActionBar(parse(rawMessage.get(0))));
    }

    public void sendActionBar(Audience audience, Placeholders placeholders) {
        if (audience == null || rawMessage.isEmpty()) return;
        if (placeholders == null) {
            sendActionBar(audience);
            return;
        }
        runOnMain(() -> {
            List<String> expanded = placeholders.apply(rawMessage.get(0));
            if (expanded.isEmpty()) return;
            audience.sendActionBar(parse(expanded.get(0)));
        });
    }

    // 
    // Внутренние методы
    // 

    private void sendSync(Player player) {
        for (Component component : getParsedCached()) {
            player.sendMessage(component);
        }
    }

    private void sendSync(CommandSender sender) {
        for (Component component : getParsedCached()) {
            sender.sendMessage(LEGACY.serialize(component));
        }
    }

    private void sendSync(Player player, Placeholders placeholders) {
        for (String line : rawMessage) {
            for (String msg : placeholders.apply(line)) {
                player.sendMessage(parse(msg));
            }
        }
    }

    private void sendSync(CommandSender sender, Placeholders placeholders) {
        for (String line : rawMessage) {
            for (String msg : placeholders.apply(line)) {
                sender.sendMessage(LEGACY.serialize(parse(msg)));
            }
        }
    }

    private void runOnMain(Runnable task) {
        if (Bukkit.isPrimaryThread()) {
            task.run();
        } else {
            Bukkit.getScheduler().runTask(plugin, task);
        }
    }

    // 
    // Геттеры
    // 

    /** Первая стока без ЛЮБЫХ плейсхолдеров */
    public String getFirstStringClean() {
        if (rawMessage.isEmpty()) return null;
        return stripColorTags(rawMessage.get(0));
    }

    /** Первая строка, полностью обработанная парсером, без подстановки плейсхолдеров. */
    public String getFirstStringParsed() {
        if (rawMessage.isEmpty()) return null;
        return PLAIN.serialize(parse(rawMessage.get(0)));
    }

    /** Первая строка, полностью обработанная парсером, с подстановкой плейсхолдеров. */
    public String getFirstStringParsed(@NotNull Placeholders placeholders) {
        if (rawMessage.isEmpty()) return null;
        List<String> expanded = placeholders.apply(rawMessage.get(0));
        if (expanded.isEmpty()) return null;
        return PLAIN.serialize(parse(expanded.get(0)));
    }

    private static final Pattern MINIMESSAGE_TAG_PATTERN = Pattern.compile("</?[a-zA-Z0-9_:#]+(:[^>]*)?>");
    private static final Pattern LEGACY_COLOR_PATTERN = Pattern.compile("&[0-9a-fk-orA-FK-OR]");

    private String stripColorTags(@NotNull String input) {
        String noLegacy = LEGACY_COLOR_PATTERN.matcher(input).replaceAll("");
        return MINIMESSAGE_TAG_PATTERN.matcher(noLegacy).replaceAll("");
    }

    public List<String> getText() {
        return getText(null);
    }

    public List<String> getText(Placeholders placeholders) {
        List<String> result = new ArrayList<>();
        for (Component component : getComponents(placeholders)) {
            result.add(PLAIN.serialize(component));
        }
        return result;
    }

    public List<Component> getComponents() {
        return getComponents(null);
    }

    public List<Component> getComponents(Placeholders placeholders) {
        if (placeholders == null) {
            return getParsedCached();
        }
        List<Component> result = new ArrayList<>();
        for (String line : rawMessage) {
            for (String msg : placeholders.apply(line)) {
                result.add(parse(msg));
            }
        }
        return result;
    }

    private List<Component> getParsedCached() {
        List<Component> cache = parsedCache;
        if (cache == null) {
            cache = new ArrayList<>(rawMessage.size());
            for (String line : rawMessage) {
                cache.add(parse(line));
            }
            cache = Collections.unmodifiableList(cache);
            parsedCache = cache;
        }
        return cache;
    }

    @Override
    public String toString() {
        return String.join("\n", rawMessage);
    }

    //
    // Парсинг 
    //

    private @NotNull Component parse(@NotNull String message) {
        String normalized = normalizeLegacyTags(message);
        Component legacy = LEGACY.deserialize(normalized);
        return MINI_MESSAGE.deserialize(normalized).mergeStyle(legacy);
    }

    private String normalizeLegacyTags(@NotNull String input) {
        Matcher matcher = LEGACY_TAG_PATTERN.matcher(input);
        StringBuilder buffer = new StringBuilder();
        while (matcher.find()) {
            String tag = matcher.group(1).toLowerCase(Locale.ROOT);
            String replacement = matcher.group().startsWith("</") ? "</" + tag + ">" : "<" + tag + ">";
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }
}