package org.springframework.roo.addon.finder;

import static org.springframework.roo.shell.OptionContexts.UPDATE_PROJECT;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.text.StrTokenizer;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.shell.CliAvailabilityIndicator;
import org.springframework.roo.shell.CliCommand;
import org.springframework.roo.shell.CliOption;
import org.springframework.roo.shell.CommandMarker;

/**
 * Commands for the 'finder' add-on to be used by the ROO shell.
 * 
 * @author Stefan Schmidt
 * @since 1.0
 */
@Component
@Service
public class FinderCommands implements CommandMarker {

    @Reference private FinderOperations finderOperations;

    @CliCommand(value = "finder add", help = "Install finders in the given target (must be an entity)")
    public void installFinders(
            @CliOption(key = "class", mandatory = false, unspecifiedDefaultValue = "*", optionContext = UPDATE_PROJECT, help = "The controller or entity for which the finders are generated") final JavaType typeName,
            @CliOption(key = { "finderName", "" }, mandatory = true, help = "The finder string as generated with the 'finder list' command") final JavaSymbolName finderName) {

        finderOperations.installFinder(typeName, finderName);
    }

    @CliAvailabilityIndicator({ "finder list", "finder add" })
    public boolean isFinderCommandAvailable() {
        return finderOperations.isFinderInstallationPossible();
    }

    @CliCommand(value = "finder list", help = "List all finders for a given target (must be an entity)")
    public SortedSet<String> listFinders(
            @CliOption(key = "class", mandatory = false, unspecifiedDefaultValue = "*", optionContext = UPDATE_PROJECT, help = "The controller or entity for which the finders are generated") final JavaType typeName,
            @CliOption(key = { "", "depth" }, mandatory = false, unspecifiedDefaultValue = "1", specifiedDefaultValue = "1", help = "The depth of attribute combinations to be generated for the finders") final Integer depth,
            @CliOption(key = "filter", mandatory = false, help = "A comma separated list of strings that must be present in a filter to be included") final String filter) {

        Validate.isTrue(depth >= 1, "Depth must be at least 1");
        Validate.isTrue(depth <= 3, "Depth must not be greater than 3");

        final SortedSet<String> finders = finderOperations.listFindersFor(
                typeName, depth);
        if (StringUtils.isBlank(filter)) {
            return finders;
        }

        final Set<String> requiredEntries = new HashSet<String>();
        final String[] filterTokens = new StrTokenizer(filter, ",")
                .getTokenArray();
        for (final String requiredString : filterTokens) {
            requiredEntries.add(requiredString.toLowerCase());
        }
        if (requiredEntries.isEmpty()) {
            return finders;
        }

        final SortedSet<String> result = new TreeSet<String>();
        for (final String finder : finders) {
            required: for (final String requiredEntry : requiredEntries) {
                if (finder.toLowerCase().contains(requiredEntry)) {
                    result.add(finder);
                    break required;
                }
            }
        }
        return result;
    }
}
