package com.rubix.pension.reports.sort;

import com.rubix.pension.reports.dto.AggregatedBalanceRowDto;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AggregatedBalanceRowComparatorTest {

    @Test
    void matchesAccessGeneralOrderForKnownPrefix() {
        List<AggregatedBalanceRowDto> rows = new ArrayList<>(List.of(
                row("مياده حسين عبدالرحمن ابراهيم", "EGP", "C000010_1"),
                row(" ايمان عبد النبي محمد حماده", "EGP", "C000010_295"),
                row("إبراهيم عبد الستار إبراهيم حسين", null, "C000010_2561"),
                row("إبراهيم عبد الستار إبراهيم حسين", "EGP", "C000010_2561"),
                row(" سمر شوقي شفيق يوسف احمد", null, "C000010_2828"),
                row(" سمر شوقي شفيق يوسف احمد", "EGP", "C000010_2828"),
                row("أحمد أبوالوفا محمد ابراهيم", "EGP", "C000010_1302"),
                row("آيه أحمد حماده أحمد خبيز", "EGP", "C000010_3317"),
                row("أيه عيد فتحى محمد جبر", null, "C000010_2932"),
                row("ابانوب اسحاق يوسف فلتاؤوس", null, "C000010_3465")
        ));

        rows.sort(AggregatedBalanceRowComparator.INSTANCE);

        assertEquals(" ايمان عبد النبي محمد حماده", rows.get(0).getFullName());
        assertEquals("EGP", rows.get(0).getCurrency());
        assertEquals(" سمر شوقي شفيق يوسف احمد", rows.get(1).getFullName());
        assertEquals(null, rows.get(1).getCurrency());
        assertEquals(" سمر شوقي شفيق يوسف احمد", rows.get(2).getFullName());
        assertEquals("EGP", rows.get(2).getCurrency());
        assertEquals("إبراهيم عبد الستار إبراهيم حسين", rows.get(3).getFullName());
        assertEquals(null, rows.get(3).getCurrency());
        assertEquals("إبراهيم عبد الستار إبراهيم حسين", rows.get(4).getFullName());
        assertEquals("EGP", rows.get(4).getCurrency());
        assertEquals("أحمد أبوالوفا محمد ابراهيم", rows.get(5).getFullName());
        assertEquals("آيه أحمد حماده أحمد خبيز", rows.get(6).getFullName());
        assertEquals("أيه عيد فتحى محمد جبر", rows.get(7).getFullName());
        assertEquals("ابانوب اسحاق يوسف فلتاؤوس", rows.get(8).getFullName());
        assertEquals("مياده حسين عبدالرحمن ابراهيم", rows.get(9).getFullName());
    }

    private static AggregatedBalanceRowDto row(String fullName, String currency, String employeeNumber) {
        AggregatedBalanceRowDto row = new AggregatedBalanceRowDto();
        row.setFullName(fullName);
        row.setCurrency(currency);
        row.setEmployeeNumber(employeeNumber);
        return row;
    }
}
