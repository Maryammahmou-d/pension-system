package com.rubix.pension.reports.sort;

import com.rubix.pension.reports.dto.AggregatedBalanceRowDto;

import java.util.Comparator;

/**
 * Access Step_1 row order: Full_Name (General), Currency nulls first, Employee_Number.
 */
public final class AggregatedBalanceRowComparator implements Comparator<AggregatedBalanceRowDto> {

    public static final AggregatedBalanceRowComparator INSTANCE = new AggregatedBalanceRowComparator();

    private final Comparator<String> nameComparator = AccessGeneralNameComparator.INSTANCE;

    private AggregatedBalanceRowComparator() {
    }

    @Override
    public int compare(AggregatedBalanceRowDto left, AggregatedBalanceRowDto right) {
        int byName = nameComparator.compare(left.getFullName(), right.getFullName());
        if (byName != 0) {
            return byName;
        }

        int byCurrency = compareCurrencyNullsFirst(left.getCurrency(), right.getCurrency());
        if (byCurrency != 0) {
            return byCurrency;
        }

        return nullSafeCompare(left.getEmployeeNumber(), right.getEmployeeNumber());
    }

    private static int compareCurrencyNullsFirst(String left, String right) {
        boolean leftNull = isBlank(left);
        boolean rightNull = isBlank(right);
        if (leftNull && !rightNull) {
            return -1;
        }
        if (!leftNull && rightNull) {
            return 1;
        }
        if (leftNull) {
            return 0;
        }
        return left.compareTo(right);
    }

    private static int nullSafeCompare(String left, String right) {
        if (left == null && right == null) {
            return 0;
        }
        if (left == null) {
            return -1;
        }
        if (right == null) {
            return 1;
        }
        return left.compareTo(right);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
