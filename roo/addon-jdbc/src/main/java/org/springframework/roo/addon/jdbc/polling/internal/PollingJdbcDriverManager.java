package org.springframework.roo.addon.jdbc.polling.internal;

import java.sql.Driver;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.ReferenceStrategy;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.addon.jdbc.JdbcDriverManager;
import org.springframework.roo.addon.jdbc.polling.JdbcDriverProvider;
import org.springframework.roo.support.api.AddOnSearch;
import org.springframework.roo.support.logging.HandlerUtils;

/**
 * Polls all OSGi-located {@link JdbcDriverProvider} instances and returns the
 * first JDBC driver provided by an instance.
 * <p>
 * Failing the location of a suitable {@link JdbcDriverProvider}, automatically
 * suggests an add-on which may be able to provide the driver as a console
 * message.
 * 
 * @author Alan Stewart
 * @author Ben Alex
 * @since 1.1
 */
@Component
@Service
@Reference(name = "jdbcDriverProvider", strategy = ReferenceStrategy.EVENT, policy = ReferencePolicy.DYNAMIC, referenceInterface = JdbcDriverProvider.class, cardinality = ReferenceCardinality.OPTIONAL_MULTIPLE)
public class PollingJdbcDriverManager implements JdbcDriverManager {

    private static final Logger LOGGER = HandlerUtils
            .getLogger(PollingJdbcDriverManager.class);

    @Reference private AddOnSearch addOnSearch;
    private final Set<JdbcDriverProvider> providers = new HashSet<JdbcDriverProvider>();

    protected void bindJdbcDriverProvider(final JdbcDriverProvider listener) {
        synchronized (providers) {
            providers.add(listener);
        }
    }

    public Driver loadDriver(final String driverClassName,
            final boolean displayAddOns) throws RuntimeException {
        Validate.notBlank(driverClassName, "Driver class name required");
        synchronized (providers) {
            for (final JdbcDriverProvider provider : providers) {
                final Driver driver = provider.loadDriver(driverClassName);
                if (driver != null) {
                    return driver;
                }
            }

            if (!displayAddOns) {
                // Caller requested add-on information not be displayed (might
                // be in a TAB assist section etc)
                return null;
            }

            // No implementation could provide it

            // Compute a suitable search term for a JDBC driver
            final String searchTerms = "#jdbcdriver,driverclass:"
                    + driverClassName;

            // Do a silent (console message free) lookup of matches
            final Integer matches = addOnSearch.searchAddOns(false,
                    searchTerms, false, 1, 99, false, false, false, null);

            // Render to screen if required
            if (matches == null) {
                LOGGER.info("Spring Roo automatic add-on discovery service currently unavailable");
            }
            else if (matches == 0) {
                LOGGER.info("addon search --requiresDescription \""
                        + searchTerms + "\" found no matches");
            }
            else if (matches > 0) {
                LOGGER.info("Located add-on" + (matches == 1 ? "" : "s")
                        + " that may offer this JDBC driver");
                addOnSearch.searchAddOns(true, searchTerms, false, 1, 99,
                        false, false, false, null);
            }

            return null;
        }
    }

    protected void unbindJdbcDriverProvider(final JdbcDriverProvider listener) {
        synchronized (providers) {
            providers.remove(listener);
        }
    }
}
