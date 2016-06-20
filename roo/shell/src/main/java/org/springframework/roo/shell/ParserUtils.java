package org.springframework.roo.shell;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.Validate;

import static org.springframework.roo.shell.CliOption.*;

/**
 * Utilities for parsing.
 * 
 * @author Ben Alex
 * @since 1.0
 */
public class ParserUtils {

    private static void store(final Map<String, String> results,
            final StringBuilder currentOption, final StringBuilder currentValue) {
        if (currentOption.length() > 0) {
            // There is an option marker
            final String option = currentOption.toString();
            Validate.isTrue(!results.containsKey(option),
                    "You cannot specify option '" + option
                            + "' more than once in a single command");
            results.put(option, currentValue.toString());
        }
        else {
            // There was no option marker, so verify this isn't the first
            Validate.isTrue(
                    !results.containsKey(""),
                    "You cannot add more than one default option ('%s') in a single command",
                    currentValue.toString());
            results.put("", currentValue.toString());
        }
    }

    /**
     * Converts a particular buffer into a tokenized structure.
     * <p>
     * Properly treats double quotes (") as option delimiters.
     * <p>
     * Expects option names to be preceded by a single or double dash. We call
     * this an "option marker".
     * <p>
     * Treats spaces as the default option tokenizer.
     * <p>
     * Any token without an option marker is considered the default. The default
     * is returned in the Map as an element with an empty string key (""). There
     * can only be a single default.
     * 
     * @param remainingBuffer to tokenize
     * @return a Map where keys are the option names (minus any dashes) and
     *         values are the option values (any double-quotes are removed)
     */
    public static Map<String, String> tokenize(final String remainingBuffer) {
        Validate.notNull(remainingBuffer,
                "Remaining buffer cannot be null, although it can be empty");
        final Map<String, String> result = new LinkedHashMap<String, String>();
        StringBuilder currentOption = new StringBuilder();
        StringBuilder currentValue = new StringBuilder();
        boolean inQuotes = false;

        // Verify correct number of double quotes are present
        int count = 0;
        for (final char c : remainingBuffer.toCharArray()) {
            if ('"' == c) {
                count++;
            }
        }
        Validate.isTrue(count % 2 == 0,
                "Cannot have an unbalanced number of quotation marks");

        if ("".equals(remainingBuffer.trim())) {
            // They've not specified anything, so exit now
            return result;
        }

        final String[] split = remainingBuffer.split(" ");
        for (int i = 0; i < split.length; i++) {
            final String currentToken = split[i];

            if (currentToken.startsWith("\"") && currentToken.endsWith("\"")
                    && currentToken.length() > 1) {
                final String tokenLessDelimiters = currentToken.substring(1,
                        currentToken.length() - 1);
                currentValue.append(tokenLessDelimiters);

                // If the current value is an empty string that means the
                // user has explicitly set it as such so mark it as empty
                // so that it doesn't get replaced by null or a default
                // value during parsing.
                if ("".equals(currentValue.toString())) {
                    currentValue.append(EMPTY);
                }

                // Store this token
                store(result, currentOption, currentValue);
                currentOption = new StringBuilder();
                currentValue = new StringBuilder();
                continue;
            }

            if (inQuotes) {
                // We're only interested in this token series ending
                if (currentToken.endsWith("\"")) {
                    final String tokenLessDelimiters = currentToken.substring(
                            0, currentToken.length() - 1);
                    currentValue.append(" ").append(tokenLessDelimiters);
                    inQuotes = false;

                    // Store this now-ended token series
                    store(result, currentOption, currentValue);
                    currentOption = new StringBuilder();
                    currentValue = new StringBuilder();
                }
                else {
                    // The current token series has not ended
                    currentValue.append(" ").append(currentToken);
                }
                continue;
            }

            if (currentToken.startsWith("\"")) {
                // We're about to start a new delimited token
                final String tokenLessDelimiters = currentToken.substring(1);
                currentValue.append(tokenLessDelimiters);
                inQuotes = true;
                continue;
            }

            if (currentToken.trim().equals("")) {
                // It's simply empty, so ignore it (ROO-23)
                continue;
            }

            if (currentToken.startsWith("--")) {
                // We're about to start a new option marker
                // First strip all of the - or -- or however many there are
                final int lastIndex = currentToken.lastIndexOf("-");
                final String tokenLessDelimiters = currentToken
                        .substring(lastIndex + 1);
                currentOption.append(tokenLessDelimiters);

                // Store this token if it's the last one, or the next token
                // starts with a "-"
                if (i + 1 == split.length) {
                    // We're at the end of the tokens, so store this one and
                    // stop processing
                    store(result, currentOption, currentValue);
                    break;
                }

                if (split[i + 1].startsWith("-")) {
                    // A new token is being started next iteration, so store
                    // this one now
                    store(result, currentOption, currentValue);
                    currentOption = new StringBuilder();
                    currentValue = new StringBuilder();
                }

                continue;
            }

            // We must be in a standard token

            // If the standard token has no option name, we allow it to contain
            // unquoted spaces
            if (currentOption.length() == 0) {
                if (currentValue.length() > 0) {
                    // Existing content, so add a space first
                    currentValue.append(" ");
                }
                currentValue.append(currentToken);

                // Store this token if it's the last one, or the next token
                // starts with a "-"
                if (i + 1 == split.length) {
                    // We're at the end of the tokens, so store this one and
                    // stop processing
                    store(result, currentOption, currentValue);
                    break;
                }

                if (split[i + 1].startsWith("--")) {
                    // A new token is being started next iteration, so store
                    // this one now
                    store(result, currentOption, currentValue);
                    currentOption = new StringBuilder();
                    currentValue = new StringBuilder();
                }

                continue;
            }

            // This is an ordinary token, so store it now
            currentValue.append(currentToken);
            store(result, currentOption, currentValue);
            currentOption = new StringBuilder();
            currentValue = new StringBuilder();
        }

        // Strip out an empty default option, if it was returned (ROO-379)
        if (result.containsKey("") && result.get("").trim().equals("")) {
            result.remove("");
        }

        return result;
    }

    private ParserUtils() {
    }
}
