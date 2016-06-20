package org.springframework.roo.support.logging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.apache.commons.lang3.Validate;

/**
 * Defers the publication of JDK {@link LogRecord} instances until a target
 * {@link Handler} is registered.
 * <p>
 * This class is useful if a target {@link Handler} cannot be instantiated
 * before {@link LogRecord} instances are being published. This may be the case
 * if the target {@link Handler} requires the establishment of complex
 * publication infrastructure such as a GUI, message queue, IoC container and
 * the establishment of that infrastructure may produce log messages that should
 * ultimately be delivered to the target {@link Handler}.
 * <p>
 * In recognition that sometimes the target {@link Handler} may never be
 * registered (perhaps due to failures configuring its supporting
 * infrastructure), this class supports a fallback mode. When in fallback mode,
 * a fallback {@link Handler} will receive all previous and future
 * {@link LogRecord} instances. Fallback mode is automatically triggered if a
 * {@link LogRecord} is published at the fallback {@link Level}. Fallback mode
 * is also triggered if the {@link #flush()} or {@link #close()} method is
 * involved and the target {@link Handler} has never been registered.
 * 
 * @author Ben Alex
 * @since 1.0
 */
public class DeferredLogHandler extends Handler {

    private final Handler fallbackHandler;
    private boolean fallbackMode = false;
    private final Level fallbackPushLevel;
    private final List<LogRecord> logRecords = Collections
            .synchronizedList(new ArrayList<LogRecord>());
    private Handler targetHandler;

    /**
     * Creates an instance that will publish all recorded {@link LogRecord}
     * instances to the specified fallback {@link Handler} if an event of the
     * specified {@link Level} is received.
     * 
     * @param fallbackHandler to publish events to (mandatory)
     * @param fallbackPushLevel the level which will trigger an event
     *            publication (mandatory)
     */
    public DeferredLogHandler(final Handler fallbackHandler,
            final Level fallbackPushLevel) {
        Validate.notNull(fallbackHandler, "Fallback handler required");
        Validate.notNull(fallbackPushLevel, "Fallback push level required");
        this.fallbackHandler = fallbackHandler;
        this.fallbackPushLevel = fallbackPushLevel;
    }

    @Override
    public void close() throws SecurityException {
        if (targetHandler == null) {
            fallbackMode = true;
        }
        if (fallbackMode) {
            publishLogRecordsTo(fallbackHandler);
            fallbackHandler.close();
            return;
        }
        targetHandler.close();
    }

    @Override
    public void flush() {
        if (targetHandler == null) {
            fallbackMode = true;
        }
        if (fallbackMode) {
            publishLogRecordsTo(fallbackHandler);
            fallbackHandler.flush();
            return;
        }
        targetHandler.flush();
    }

    /**
     * @return the target {@link Handler}, or null if there is no target
     *         {@link Handler} defined so far
     */
    public Handler getTargetHandler() {
        return targetHandler;
    }

    /**
     * Stores the log record internally.
     */
    @Override
    public void publish(final LogRecord record) {
        if (!isLoggable(record)) {
            return;
        }
        if (fallbackMode) {
            fallbackHandler.publish(record);
            return;
        }
        if (targetHandler != null) {
            targetHandler.publish(record);
            return;
        }
        synchronized (logRecords) {
            logRecords.add(record);
        }
        if (!fallbackMode
                && record.getLevel().intValue() >= fallbackPushLevel.intValue()) {
            fallbackMode = true;
            publishLogRecordsTo(fallbackHandler);
        }
    }

    private void publishLogRecordsTo(final Handler destination) {
        synchronized (logRecords) {
            for (final LogRecord record : logRecords) {
                destination.publish(record);
            }
            logRecords.clear();
        }
    }

    public void setTargetHandler(final Handler targetHandler) {
        Validate.notNull(targetHandler, "Must specify a target handler");
        this.targetHandler = targetHandler;
        if (!fallbackMode) {
            publishLogRecordsTo(this.targetHandler);
        }
    }
}
