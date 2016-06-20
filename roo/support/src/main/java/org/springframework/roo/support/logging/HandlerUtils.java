package org.springframework.roo.support.logging;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Validate;

/**
 * Utility methods for dealing with {@link Handler} objects.
 * 
 * @author Ben Alex
 * @since 1.0
 */
public final class HandlerUtils {

    /**
     * Forces all {@link Handler} instances registered in the presented
     * {@link Logger} to be flushed.
     * 
     * @param logger to flush (required)
     * @return the number of {@link Handler}s flushed (may be 0 or above)
     */
    public static int flushAllHandlers(final Logger logger) {
        Validate.notNull(logger, "Logger is required");

        int flushed = 0;
        final Handler[] handlers = logger.getHandlers();
        if (handlers != null && handlers.length > 0) {
            for (final Handler h : handlers) {
                flushed++;
                h.flush();
            }
        }

        return flushed;
    }

    /**
     * Obtains a {@link Logger} that guarantees to set the {@link Level} to
     * {@link Level#FINE} if it is part of org.springframework.roo.
     * Unfortunately this is needed due to a regression in JDK 1.6.0_18 as per
     * issue ROO-539.
     * 
     * @param clazz to retrieve the logger for (required)
     * @return the logger, which will at least of {@link Level#FINE} if no level
     *         was specified
     */
    public static Logger getLogger(final Class<?> clazz) {
        Validate.notNull(clazz, "Class required");
        final Logger logger = Logger.getLogger(clazz.getName());
        if (logger.getLevel() == null
                && clazz.getName().startsWith("org.springframework.roo")) {
            logger.setLevel(Level.FINE);
        }
        return logger;
    }

    /**
     * Registers the presented target {@link Handler} against any
     * {@link DeferredLogHandler} encountered in the presented {@link Logger}.
     * <p>
     * Generally this method is used on {@link Logger} instances that have
     * previously been presented to the
     * {@link #wrapWithDeferredLogHandler(Logger, Level)} method.
     * <p>
     * The method will return a count of how many {@link DeferredLogHandler}
     * instances it detected. Note that no attempt is made to distinguish
     * between instances already possessing the intended target {@link Handler}
     * or those already possessing any target {@link Handler} at all. This
     * method always overwrites the target {@link Handler} and the returned
     * count represents how many overwrites took place.
     * 
     * @param logger to introspect for {@link DeferredLogHandler} instances
     *            (required)
     * @param target to set as the target {@link Handler}
     * @return number of {@link DeferredLogHandler} instances detected and
     *         updated (may be 0 if none found)
     */
    public static int registerTargetHandler(final Logger logger,
            final Handler target) {
        Validate.notNull(logger, "Logger is required");
        Validate.notNull(target, "Target handler is required");

        int replaced = 0;
        final Handler[] handlers = logger.getHandlers();
        if (handlers != null && handlers.length > 0) {
            for (final Handler h : handlers) {
                if (h instanceof DeferredLogHandler) {
                    replaced++;
                    final DeferredLogHandler defLogger = (DeferredLogHandler) h;
                    defLogger.setTargetHandler(target);
                }
            }
        }

        return replaced;
    }

    /**
     * Replaces each {@link Handler} defined against the presented
     * {@link Logger} with {@link DeferredLogHandler}.
     * <p>
     * This is useful for ensuring any {@link Handler} defaults defined by the
     * user are preserved and treated as the {@link DeferredLogHandler}
     * "fallback" {@link Handler} if the indicated severity {@link Level} is
     * encountered.
     * <p>
     * This method will create a {@link ConsoleHandler} if the presented
     * {@link Logger} has no current {@link Handler}.
     * 
     * @param logger to introspect and replace the {@link Handler}s for
     *            (required)
     * @param fallbackSeverity to trigger fallback mode (required)
     * @return the number of {@link DeferredLogHandler}s now registered against
     *         the {@link Logger} (guaranteed to be 1 or above)
     */
    public static int wrapWithDeferredLogHandler(final Logger logger,
            final Level fallbackSeverity) {
        Validate.notNull(logger, "Logger is required");
        Validate.notNull(fallbackSeverity, "Fallback severity is required");

        final List<DeferredLogHandler> newHandlers = new ArrayList<DeferredLogHandler>();

        // Create DeferredLogHandlers for each Handler in presented Logger
        final Handler[] handlers = logger.getHandlers();
        if (handlers != null && handlers.length > 0) {
            for (final Handler h : handlers) {
                logger.removeHandler(h);
                newHandlers.add(new DeferredLogHandler(h, fallbackSeverity));
            }
        }

        // Create a default DeferredLogHandler if no Handler was defined in the
        // presented Logger
        if (newHandlers.isEmpty()) {
            final ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setFormatter(new Formatter() {
                @Override
                public String format(final LogRecord record) {
                    return record.getMessage() + IOUtils.LINE_SEPARATOR;
                }
            });
            newHandlers.add(new DeferredLogHandler(consoleHandler,
                    fallbackSeverity));
        }

        // Add the new DeferredLogHandlers to the presented Logger
        for (final DeferredLogHandler h : newHandlers) {
            logger.addHandler(h);
        }

        return newHandlers.size();
    }

    /**
     * Constructor is private to prevent instantiation
     * 
     * @since 1.2.0
     */
    private HandlerUtils() {
    }
}
