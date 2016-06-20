package org.springframework.roo.support.ant;

import java.util.Map;

/**
 * Package-protected helper class for {@link AntPathMatcher}. Tests whether or
 * not a string matches against a pattern. The pattern may contain special
 * characters:<br>
 * '*' means zero or more characters<br>
 * '?' means one and only one character, '{' and '}' indicate a uri template
 * pattern
 * 
 * @author Arjen Poutsma
 * @since 3.0
 */
class AntPatchStringMatcher {

    private char ch;
    private final char[] patArr;
    private int patIdxEnd;
    private int patIdxStart = 0;
    private final char[] strArr;
    private int strIdxEnd;
    private int strIdxStart = 0;
    private final Map<String, String> uriTemplateVariables;

    /** Constructs a new instance of the <code>AntPatchStringMatcher</code>. */
    AntPatchStringMatcher(final String pattern, final String str,
            final Map<String, String> uriTemplateVariables) {
        patArr = pattern.toCharArray();
        strArr = str.toCharArray();
        this.uriTemplateVariables = uriTemplateVariables;
        patIdxEnd = patArr.length - 1;
        strIdxEnd = strArr.length - 1;
    }

    private void addTemplateVariable(final int curlyIdxStart,
            final int curlyIdxEnd, final int valIdxStart, final int valIdxEnd) {
        if (uriTemplateVariables != null) {
            final String varName = new String(patArr, curlyIdxStart + 1,
                    curlyIdxEnd - curlyIdxStart - 1);
            final String varValue = new String(strArr, valIdxStart, valIdxEnd
                    - valIdxStart + 1);
            uriTemplateVariables.put(varName, varValue);
        }
    }

    private boolean allCharsUsed() {
        return strIdxStart > strIdxEnd;
    }

    private boolean consecutiveStars(final int patIdxTmp) {
        if (patIdxTmp == patIdxStart + 1 && patArr[patIdxStart] == '*'
                && patArr[patIdxTmp] == '*') {
            // Two stars next to each other, skip the first one.
            patIdxStart++;
            return true;
        }
        return false;
    }

    private boolean doShortcut() {
        if (patIdxEnd != strIdxEnd) {
            return false; // Pattern and string do not have the same size
        }
        for (int i = 0; i <= patIdxEnd; i++) {
            ch = patArr[i];
            if (ch != '?') {
                if (ch != strArr[i]) {
                    return false;// Character mismatch
                }
            }
        }
        return true; // String matches against pattern
    }

    private int findClosingCurly() {
        for (int i = patIdxStart + 1; i <= patIdxEnd; i++) {
            if (patArr[i] == '}') {
                return i;
            }
        }
        return -1;
    }

    private int findNextStarOrCurly() {
        for (int i = patIdxStart + 1; i <= patIdxEnd; i++) {
            if (patArr[i] == '*' || patArr[i] == '{') {
                return i;
            }
        }
        return -1;
    }

    private boolean matchAfterLastStarOrCurly() {
        while ((ch = patArr[patIdxEnd]) != '*' && ch != '}'
                && strIdxStart <= strIdxEnd) {
            if (ch != '?') {
                if (ch != strArr[strIdxEnd]) {
                    return false;
                }
            }
            patIdxEnd--;
            strIdxEnd--;
        }
        return true;
    }

    private boolean matchBeforeFirstStarOrCurly() {
        while ((ch = patArr[patIdxStart]) != '*' && ch != '{'
                && strIdxStart <= strIdxEnd) {
            if (ch != '?') {
                if (ch != strArr[strIdxStart]) {
                    return false;
                }
            }
            patIdxStart++;
            strIdxStart++;
        }
        return true;
    }

    /**
     * Main entry point.
     * 
     * @return <code>true</code> if the string matches against the pattern, or
     *         <code>false</code> otherwise.
     */
    boolean matchStrings() {
        if (shortcutPossible()) {
            return doShortcut();
        }
        if (patternContainsOnlyStar()) {
            return true;
        }
        if (patternContainsOneTemplateVariable()) {
            addTemplateVariable(0, patIdxEnd, 0, strIdxEnd);
            return true;
        }
        if (!matchBeforeFirstStarOrCurly()) {
            return false;
        }
        if (allCharsUsed()) {
            return onlyStarsLeft();
        }
        if (!matchAfterLastStarOrCurly()) {
            return false;
        }
        if (allCharsUsed()) {
            return onlyStarsLeft();
        }
        // Process pattern between stars. padIdxStart and patIdxEnd point always
        // to a '*'.
        while (patIdxStart != patIdxEnd && strIdxStart <= strIdxEnd) {
            int patIdxTmp;
            if (patArr[patIdxStart] == '{') {
                patIdxTmp = findClosingCurly();
                addTemplateVariable(patIdxStart, patIdxTmp, strIdxStart,
                        strIdxEnd);
                patIdxStart = patIdxTmp + 1;
                strIdxStart = strIdxEnd + 1;
                continue;
            }
            patIdxTmp = findNextStarOrCurly();
            if (consecutiveStars(patIdxTmp)) {
                continue;
            }
            // Find the pattern between padIdxStart & padIdxTmp in str between
            // strIdxStart & strIdxEnd
            final int patLength = patIdxTmp - patIdxStart - 1;
            final int strLength = strIdxEnd - strIdxStart + 1;
            int foundIdx = -1;
            strLoop: for (int i = 0; i <= strLength - patLength; i++) {
                for (int j = 0; j < patLength; j++) {
                    ch = patArr[patIdxStart + j + 1];
                    if (ch != '?') {
                        if (ch != strArr[strIdxStart + i + j]) {
                            continue strLoop;
                        }
                    }
                }

                foundIdx = strIdxStart + i;
                break;
            }

            if (foundIdx == -1) {
                return false;
            }

            patIdxStart = patIdxTmp;
            strIdxStart = foundIdx + patLength;
        }

        return onlyStarsLeft();
    }

    private boolean onlyStarsLeft() {
        for (int i = patIdxStart; i <= patIdxEnd; i++) {
            if (patArr[i] != '*') {
                return false;
            }
        }
        return true;
    }

    private boolean patternContainsOneTemplateVariable() {
        if (patIdxEnd >= 2 && patArr[0] == '{' && patArr[patIdxEnd] == '}') {
            for (int i = 1; i < patIdxEnd; i++) {
                if (patArr[i] == '}') {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private boolean patternContainsOnlyStar() {
        return patIdxEnd == 0 && patArr[0] == '*';
    }

    private boolean shortcutPossible() {
        for (final char ch : patArr) {
            if (ch == '*' || ch == '{' || ch == '}') {
                return false;
            }
        }
        return true;
    }
}
