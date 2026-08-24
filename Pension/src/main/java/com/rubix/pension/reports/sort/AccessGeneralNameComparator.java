package com.rubix.pension.reports.sort;

import java.util.Comparator;

/**
 * Approximates Microsoft Access/Jet "General" (CollatingOrder 1033) sort for Arabic Full_Name values.
 * Validated against Access Step_1 output for C000010.
 */
public final class AccessGeneralNameComparator implements Comparator<String> {

    public static final AccessGeneralNameComparator INSTANCE = new AccessGeneralNameComparator();

    private AccessGeneralNameComparator() {
    }

    @Override
    public int compare(String left, String right) {
        String a = left == null ? "" : left;
        String b = right == null ? "" : right;

        int primary = comparePrimary(a, b);
        if (primary != 0) {
            return primary;
        }

        int secondary = compareSecondary(a, b);
        if (secondary != 0) {
            return secondary;
        }

        return a.compareTo(b);
    }

    private static int comparePrimary(String left, String right) {
        int max = Math.max(left.length(), right.length());
        for (int i = 0; i < max; i += 1) {
            char leftChar = i < left.length() ? left.charAt(i) : 0;
            char rightChar = i < right.length() ? right.charAt(i) : 0;
            char leftKey = primaryKey(leftChar);
            char rightKey = primaryKey(rightChar);
            if (leftKey != rightKey) {
                return Character.compare(leftKey, rightKey);
            }
        }
        return 0;
    }

    private static int compareSecondary(String left, String right) {
        int max = Math.max(left.length(), right.length());
        for (int i = 0; i < max; i += 1) {
            int leftWeight = i < left.length() ? secondaryWeight(left.charAt(i)) : 0;
            int rightWeight = i < right.length() ? secondaryWeight(right.charAt(i)) : 0;
            if (leftWeight != rightWeight) {
                return Integer.compare(leftWeight, rightWeight);
            }
        }
        return 0;
    }

    /**
     * Latin markers sort after space and before Arabic letters so leading-space names stay first.
     */
    private static char primaryKey(char ch) {
        return switch (ch) {
            case '\u0625' -> 'B'; // إ
            case '\u0623', '\u0622' -> 'C'; // أ, آ
            case '\u0627', '\u0671' -> 'D'; // ا, ٱ
            default -> ch;
        };
    }

    /** When primary keys tie, آ sorts before أ. */
    private static int secondaryWeight(char ch) {
        return switch (ch) {
            case '\u0622' -> 0; // آ
            case '\u0623' -> 1; // أ
            default -> 0;
        };
    }
}
