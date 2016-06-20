package org.springframework.roo.addon.logging;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.shell.CliAvailabilityIndicator;
import org.springframework.roo.shell.CliCommand;
import org.springframework.roo.shell.CliOption;
import org.springframework.roo.shell.CommandMarker;
import org.springframework.roo.shell.converters.StaticFieldConverter;

/**
 * Commands for the 'logging' add-on to be used by the ROO shell.
 * 
 * @author Stefan Schmidt
 * @since 1.0
 */
@Component
@Service
public class LoggingCommands implements CommandMarker {

    @Reference private LoggingOperations loggingOperations;
    @Reference private StaticFieldConverter staticFieldConverter;

    protected void activate(final ComponentContext context) {
        staticFieldConverter.add(LoggerPackage.class);
        staticFieldConverter.add(LogLevel.class);
    }

    @CliCommand(value = "logging setup", help = "Configure logging in your project")
    public void configureLogging(
            @CliOption(key = { "", "level" }, mandatory = true, help = "The log level to configure") final LogLevel logLevel,
            @CliOption(key = "package", mandatory = false, help = "The package to append the logging level to (all by default)") final LoggerPackage loggerPackage) {

        loggingOperations.configureLogging(logLevel,
                loggerPackage == null ? LoggerPackage.ROOT : loggerPackage);
    }

    protected void deactivate(final ComponentContext context) {
        staticFieldConverter.remove(LoggerPackage.class);
        staticFieldConverter.remove(LogLevel.class);
    }

    @CliAvailabilityIndicator("logging setup")
    public boolean isConfigureLoggingAvailable() {
        return loggingOperations.isLoggingInstallationPossible();
    }
}