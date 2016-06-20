package org.springframework.roo.support.util;

import org.apache.commons.lang3.StringUtils;

/**
 * ANSI escape codes supported by JLine.
 * 
 * @author Andrew Swan
 * @since 1.2.0
 */
public enum AnsiEscapeCode {

    // These int literals are non-public constants in ANSIBuffer.ANSICodes
    BLINK(5), BOLD(1), CONCEALED(8), FG_BLACK(30), FG_BLUE(34), FG_CYAN(36), FG_GREEN(
            32), FG_MAGENTA(35), FG_RED(31), FG_WHITE(37), FG_YELLOW(33), OFF(0), REVERSE(
            7), UNDERSCORE(4);

    // Constant for the escape character
    private static final boolean ANSI_ENABLED = Boolean
            .getBoolean("roo.console.ansi");
    private static final char ESC = 27;

    public static boolean isAnsiEnabled() {
        return ANSI_ENABLED;
    }

    /**
     * Decorates the given text with the given escape codes (turning them off
     * afterwards)
     * 
     * @param text the text to decorate; can be <code>null</code>
     * @param codes
     * @return <code>null</code> if <code>null</code> is passed
     */
    public static String decorate(final String text,
            final AnsiEscapeCode... codes) {
        if (StringUtils.isEmpty(text)) {
            return text;
        }

        final StringBuilder sb = new StringBuilder();
        if (ANSI_ENABLED) {
            for (final AnsiEscapeCode code : codes) {
                sb.append(code.code);
            }
        }
        sb.append(text);
        if (codes != null && codes.length > 0 && ANSI_ENABLED) {
            sb.append(OFF.code);
        }
        return sb.toString();
    }

    final String code;

    /**
     * Constructor
     * 
     * @param code the numeric ANSI escape code
     */
    private AnsiEscapeCode(final int code) {
        // Copied from the method ANSIBuffer.ANSICodes#attrib(int)
        this.code = ESC + "[" + code + "m";
    }
}
