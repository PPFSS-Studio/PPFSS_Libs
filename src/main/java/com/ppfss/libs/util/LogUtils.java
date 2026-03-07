package com.ppfss.libs.util;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.plugin.Plugin;

import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("unused")
@Getter
public class LogUtils {
    private static boolean ANSI_ENABLED;
    @Setter
    private static boolean DEBUG_ENABLED;
    @Getter
    private static boolean CYRILLIC_ENABLED;
    @Getter
    private static Logger LOGGER;

    public static void init(Plugin plugin) {
        LOGGER = plugin.getLogger();
        ANSI_ENABLED = isAnsiEnabled();
        DEBUG_ENABLED = plugin.getConfig().getBoolean("debug", false);
        CYRILLIC_ENABLED = isCyrillicEnabled();
    }

    private static boolean isAnsiEnabled() {
        String os = System.getProperty("os.name").toLowerCase();
        boolean isWindows = os.contains("win");
        boolean hasTerm = System.getenv("TERM") != null;
        boolean hasConsole = System.console() != null;
        return !isWindows || hasTerm || hasConsole;
    }

    public static void info(String msg, Object... args) {
        log(Level.INFO, msg, ColorCode.WHITE, args);
    }

    public static void warn(String msg, Object... args) {
        log(Level.WARNING, msg, ColorCode.YELLOW, args);
    }

    public static void error(String msg, Object... args) {
        log(Level.SEVERE, msg, ColorCode.RED, args);
    }

    public static void debug(String msg, Object... args) {
        if (isDebugEnabled()) {
            log(Level.FINE, msg, ColorCode.CYAN, args);
        }
    }

    public static <T> T logExceptionally(Throwable throwable){
        error("", throwable);
        return null;
    }

    public static <T> T logExceptionally(Throwable throwable, String message){
        error(message, throwable);
        return null;
    }

    public static void log(Level level, String msg, ColorCode color, Object... args) {
        Throwable throwable = extractThrowable(args);

        Object[] logArgs = (throwable != null) ? trimLast(args) : args;

        String formattedMessage = formatMessage(msg, logArgs);
        String output = ANSI_ENABLED
                ? color.getAnsiCode() + formattedMessage + ColorCode.RESET.getAnsiCode()
                : formattedMessage;

        if (throwable != null) {
            LOGGER.log(level, output, throwable);
        } else {
            LOGGER.log(level, output);
        }
    }

    private static Throwable extractThrowable(Object[] args) {
        if (args != null && args.length > 0) {
            Object last = args[args.length - 1];
            if (last instanceof Throwable) {
                return (Throwable) last;
            }
        }
        return null;
    }

    private static Object[] trimLast(Object[] args) {
        if (args.length <= 1) return new Object[0];
        Object[] newArgs = new Object[args.length - 1];
        System.arraycopy(args, 0, newArgs, 0, args.length - 1);
        return newArgs;
    }

    private static String formatMessage(String message, Object... args) {
        if (message == null || args == null || args.length == 0) {
            return message != null ? message : "";
        }

        StringBuilder result = new StringBuilder();
        int argIndex = 0;
        int start = 0;
        int openBrace;

        while ((openBrace = message.indexOf('{', start)) != -1 && argIndex < args.length) {
            if (openBrace + 1 < message.length() && message.charAt(openBrace + 1) == '}') {
                result.append(message, start, openBrace);
                result.append(args[argIndex] != null ? args[argIndex].toString() : "null");
                start = openBrace + 2;
                argIndex++;
            } else {
                result.append(message, start, openBrace + 1);
                start = openBrace + 1;
            }
        }

        result.append(message.substring(start));
        return result.toString();
    }

    private static boolean isDebugEnabled() {
        return DEBUG_ENABLED;
    }

    private static boolean isCyrillicEnabled() {
        boolean isCyrillicConsoleSupported;

        String os = System.getProperty("os.name").toLowerCase();
        String jnuEncoding = System.getProperty("sun.jnu.encoding", "unknown");
        String lang = System.getenv("LANG");

        if (os.contains("win")) {
            isCyrillicConsoleSupported = true;
        } else {
            isCyrillicConsoleSupported = true;
            if (lang != null && !lang.toLowerCase().contains("utf-8")) {
                isCyrillicConsoleSupported = jnuEncoding.equalsIgnoreCase("UTF-8");
            }
        }

        return isCyrillicConsoleSupported;
    }

    @Getter
    public enum ColorCode {
        RESET("\u001B[0m", ""),
        RED("\u001B[31m", "&c"),
        GREEN("\u001B[32m", "&a"),
        YELLOW("\u001B[33m", "&e"),
        BLUE("\u001B[34m", "&9"),
        PURPLE("\u001B[35m", "&5"),
        CYAN("\u001B[36m", "&b"),
        WHITE("\u001B[37m", "&f");

        private final String ansiCode;
        private final String chatCode;

        ColorCode(String ansiCode, String chatCode) {
            this.ansiCode = ansiCode;
            this.chatCode = chatCode;
        }

    }
}