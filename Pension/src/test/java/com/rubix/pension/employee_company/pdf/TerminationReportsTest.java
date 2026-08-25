package com.rubix.pension.employee_company.pdf;

import com.rubix.pension.employee_company.dto.BulkTerminationResultDto;
import com.rubix.pension.employee_company.dto.TerminationFundEmployeeRow;
import com.rubix.pension.employee_company.dto.TerminationFundReportDto;
import com.rubix.pension.employee_company.dto.TerminationReportDto;
import com.rubix.pension.employee_company.dto.TerminationReportFundRow;
import com.rubix.pension.employee_company.dto.TerminationSummaryRowDto;
import com.rubix.pension.employee_company.service.TerminationBundleService;
import com.rubix.pension.employee_company.service.TerminationExcelService;
import com.rubix.pension.employee_company.service.TerminationTransactionsExcelService;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TerminationReportsTest {

    @Test
    void buildsPerEmployeePdfWithPdfMagicBytes() throws Exception {
        TerminationReportPdfService service = new TerminationReportPdfService();
        byte[] pdf = service.build(sampleReport());

        assertTrue(pdf.length > 500);
        assertEquals("%PDF", new String(pdf, 0, 4));
        assertEquals("Termination_Report_C000010_998.pdf", TerminationReportPdfService.fileName(sampleReport()));
    }

    @Test
    void buildsCompanyFundPdfWithPdfMagicBytes() throws Exception {
        TerminationFundReportPdfService service = new TerminationFundReportPdfService();
        byte[] pdf = service.build(sampleFundReport());

        assertTrue(pdf.length > 500);
        assertEquals("%PDF", new String(pdf, 0, 4));
        assertEquals("Company_Termination_C000010_F1.pdf", TerminationFundReportPdfService.fileName(sampleFundReport()));
    }

    @Test
    void buildsExcelWorkbookWithZipMagicBytes() throws Exception {
        TerminationExcelService service = new TerminationExcelService();
        byte[] xlsx = service.build(sampleBulkResult());

        assertTrue(xlsx.length > 200);
        assertEquals("PK\u0003\u0004", new String(xlsx, 0, 4));
        assertEquals("Termination_C000010.xlsx", TerminationExcelService.fileName("C000010"));
    }

    @Test
    void buildsRawTransactionExcelWorkbookWithZipMagicBytes() throws Exception {
        TerminationTransactionsExcelService service = new TerminationTransactionsExcelService();
        byte[] xlsx = service.build(sampleBulkResult());

        assertTrue(xlsx.length > 200);
        assertEquals("PK\u0003\u0004", new String(xlsx, 0, 4));
        assertEquals("Terminations_C000010.xlsx", TerminationTransactionsExcelService.fileName("C000010"));
    }

    @Test
    void bundlesEverythingIntoOneZip() throws Exception {
        TerminationBundleService bundleService = new TerminationBundleService(
                new TerminationReportPdfService(),
                new TerminationFundReportPdfService(),
                new TerminationExcelService(),
                new TerminationTransactionsExcelService(),
                new com.rubix.pension.employee_company.service.TerminationFileSaveSupport()
        );

        byte[] zip = bundleService.buildZip(sampleBulkResult());
        assertTrue(zip.length > 500);

        List<String> entries = new ArrayList<>();
        try (ZipInputStream zipIn = new ZipInputStream(new java.io.ByteArrayInputStream(zip))) {
            ZipEntry entry;
            while ((entry = zipIn.getNextEntry()) != null) {
                entries.add(entry.getName());
            }
        }

        assertTrue(entries.contains("Termination_Report_C000010_998.pdf"));
        assertTrue(entries.contains("Company_Termination_C000010_F1.pdf"));
        assertTrue(entries.contains("Termination_C000010.xlsx"));
        assertTrue(entries.contains("Terminations_C000010.xlsx"));
        assertEquals("Termination_C000010.zip", TerminationBundleService.zipFileName("C000010"));
    }

    @Test
    void savePathIsOptionalAndSkippedWhenBlank() throws Exception {
        TerminationBundleService bundleService = new TerminationBundleService(
                new TerminationReportPdfService(),
                new TerminationFundReportPdfService(),
                new TerminationExcelService(),
                new TerminationTransactionsExcelService(),
                new com.rubix.pension.employee_company.service.TerminationFileSaveSupport()
        );

        // null/blank savePath must be a no-op (frontend may not choose a folder)
        bundleService.saveToDirectory(sampleBulkResult(), null);
        bundleService.saveToDirectory(sampleBulkResult(), "   ");
    }

    @Test
    void savesEveryArtifactToDirectoryWhenSavePathProvided(@org.junit.jupiter.api.io.TempDir java.nio.file.Path tempDir) throws Exception {
        TerminationBundleService bundleService = new TerminationBundleService(
                new TerminationReportPdfService(),
                new TerminationFundReportPdfService(),
                new TerminationExcelService(),
                new TerminationTransactionsExcelService(),
                new com.rubix.pension.employee_company.service.TerminationFileSaveSupport()
        );

        bundleService.saveToDirectory(sampleBulkResult(), tempDir.toString());

        assertTrue(java.nio.file.Files.exists(tempDir.resolve("Termination_Report_C000010_998.pdf")));
        assertTrue(java.nio.file.Files.exists(tempDir.resolve("Company_Termination_C000010_F1.pdf")));
        assertTrue(java.nio.file.Files.exists(tempDir.resolve("Termination_C000010.xlsx")));
        assertTrue(java.nio.file.Files.exists(tempDir.resolve("Terminations_C000010.xlsx")));
        assertEquals("Termination_C000010.zip", TerminationBundleService.zipFileName("C000010"));
    }

    private TerminationReportDto sampleReport() {
        TerminationReportDto report = new TerminationReportDto();
        report.setId(1);
        report.setModifiedDate("2026-06-08T00:00:00Z");
        report.setSerial(1);
        report.setCompanyNumber("C000010");
        report.setCompanyName("Sample Company");
        report.setEmployeeId(998);
        report.setEmployeeNumber("C000010_998");
        report.setEmployeeName("Sample Employee");
        report.setNationalId("29908011310622");
        report.setDob("1999-08-01");
        report.setGender("F");
        report.setCategory("1");
        report.setCurrency("EGP");
        report.setPensionStartDate("2024-08-01");
        report.setTerminationDate("2026-06-08");
        report.setResignationDate("");
        report.setPaymentDate("2026-06-08");
        report.setVestingPercentage(100);
        report.setSurrenderChargesEe(0);

        List<TerminationReportFundRow> rows = new ArrayList<>();
        TerminationReportFundRow row = new TerminationReportFundRow();
        row.setFund(1);
        row.setStartingEeUnits(952.65);
        row.setStartingTotalUnits(952.65);
        row.setTransactionalEeUnits(-952.65);
        row.setTransactionalTotalUnits(-952.65);
        row.setUnitPrice(14.44);
        row.setTransactionalEeValue(-6796.68);
        row.setTransactionalErValue(-6962.21);
        row.setTransactionalTotalValue(-13758.89);
        rows.add(row);
        for (int i = 2; i <= 10; i += 1) {
            rows.add(new TerminationReportFundRow());
        }
        report.setRows(rows);

        report.setTotalTransactionalEeValue(-6796.68);
        report.setTotalTransactionalErValue(-6962.21);
        report.setTotalTransactionalValue(-13758.89);
        return report;
    }

    private TerminationFundReportDto sampleFundReport() {
        TerminationFundReportDto fundReport = new TerminationFundReportDto();
        fundReport.setFund(1);
        fundReport.setCompanyNumber("C000010");
        fundReport.setCompanyName("Sample Company");
        fundReport.setUnitPrice(14.44);
        fundReport.setTerminationDate("2026-06-08");

        TerminationFundEmployeeRow employeeRow = new TerminationFundEmployeeRow();
        employeeRow.setEmployeeNumber("C000010_998");
        employeeRow.setEmployeeName("Sample Employee");
        employeeRow.setDob("01/08/1999");
        employeeRow.setTransactionalEeValue(-6796.68);
        employeeRow.setTransactionalErValue(-6962.21);
        employeeRow.setTransactionalTotalValue(-13758.89);
        fundReport.getEmployees().add(employeeRow);
        fundReport.setEmployeeCount(1);
        fundReport.setTotalTransactionalEeValue(-6796.68);
        fundReport.setTotalTransactionalErValue(-6962.21);
        fundReport.setTotalTransactionalValue(-13758.89);
        return fundReport;
    }

    private BulkTerminationResultDto sampleBulkResult() {
        BulkTerminationResultDto result = new BulkTerminationResultDto();
        result.setCompanyNumber("C000010");
        result.setCompanyName("Sample Company");
        result.getReports().add(sampleReport());
        result.getFundReports().add(sampleFundReport());

        TerminationSummaryRowDto summary = new TerminationSummaryRowDto();
        summary.setDescription("Termination");
        summary.setPaymentDate("2026-06-08");
        summary.setCompanyNumber("C000010");
        summary.setEmployeeId(998);
        summary.setEmployeeNumber("C000010_998");
        summary.setNationalId("29908011310622");
        summary.setFullName("Sample Employee");
        summary.setDob("1999-08-01");
        summary.setGender("F");
        summary.setCurrency("EGP");
        summary.setTotalEeValue(-6796.68);
        summary.setTotalErValue(-6962.21);
        summary.setTotalValue(-13758.89);
        result.getSummaryRows().add(summary);

        result.setGrandTotalTransactionalEeValue(-6796.68);
        result.setGrandTotalTransactionalErValue(-6962.21);
        result.setGrandTotalTransactionalValue(-13758.89);
        return result;
    }
}
