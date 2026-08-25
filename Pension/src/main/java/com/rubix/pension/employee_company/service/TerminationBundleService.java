package com.rubix.pension.employee_company.service;

import com.rubix.pension.employee_company.dto.BulkTerminationResultDto;
import com.rubix.pension.employee_company.dto.TerminationFundReportDto;
import com.rubix.pension.employee_company.dto.TerminationReportDto;
import com.rubix.pension.employee_company.pdf.TerminationFundReportPdfService;
import com.rubix.pension.employee_company.pdf.TerminationReportPdfService;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Bundles every downloadable termination artifact (per-employee PDFs, per-fund company PDFs,
 * and the summary Excel workbook) for a bulk/company termination result into a single ZIP.
 * Stateless: rendered entirely from an already-computed {@link BulkTerminationResultDto}, so it
 * never re-runs (or re-triggers) the termination itself.
 */
@Service
public class TerminationBundleService {

    private final TerminationReportPdfService terminationReportPdfService;
    private final TerminationFundReportPdfService terminationFundReportPdfService;
    private final TerminationExcelService terminationExcelService;
    private final TerminationTransactionsExcelService terminationTransactionsExcelService;
    private final TerminationFileSaveSupport fileSaveSupport;

    public TerminationBundleService(
            TerminationReportPdfService terminationReportPdfService,
            TerminationFundReportPdfService terminationFundReportPdfService,
            TerminationExcelService terminationExcelService,
            TerminationTransactionsExcelService terminationTransactionsExcelService,
            TerminationFileSaveSupport fileSaveSupport
    ) {
        this.terminationReportPdfService = terminationReportPdfService;
        this.terminationFundReportPdfService = terminationFundReportPdfService;
        this.terminationExcelService = terminationExcelService;
        this.terminationTransactionsExcelService = terminationTransactionsExcelService;
        this.fileSaveSupport = fileSaveSupport;
    }

    public byte[] buildZip(BulkTerminationResultDto result) throws IOException {
        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ZipOutputStream zip = new ZipOutputStream(byteOut)) {

            for (TerminationReportDto report : result.getReports()) {
                writeEntry(zip, TerminationReportPdfService.fileName(report), terminationReportPdfService.build(report));
            }

            for (TerminationFundReportDto fundReport : result.getFundReports()) {
                writeEntry(zip, TerminationFundReportPdfService.fileName(fundReport), terminationFundReportPdfService.build(fundReport));
            }

            writeEntry(zip, TerminationExcelService.fileName(result.getCompanyNumber()), terminationExcelService.build(result));
            writeEntry(zip, TerminationTransactionsExcelService.fileName(result.getCompanyNumber()),
                    terminationTransactionsExcelService.build(result));

            zip.finish();
            return byteOut.toByteArray();
        }
    }

    /**
     * Mirrors {@link #buildZip}, but writes each artifact directly to {@code savePath} on the
     * server's filesystem instead of zipping them. No-op if {@code savePath} is blank/null.
     */
    public void saveToDirectory(BulkTerminationResultDto result, String savePath) throws IOException {
        if (fileSaveSupport.isBlank(savePath)) {
            return;
        }
        for (TerminationReportDto report : result.getReports()) {
            fileSaveSupport.save(savePath, TerminationReportPdfService.fileName(report), terminationReportPdfService.build(report));
        }
        for (TerminationFundReportDto fundReport : result.getFundReports()) {
            fileSaveSupport.save(savePath, TerminationFundReportPdfService.fileName(fundReport), terminationFundReportPdfService.build(fundReport));
        }
        fileSaveSupport.save(savePath, TerminationExcelService.fileName(result.getCompanyNumber()), terminationExcelService.build(result));
        fileSaveSupport.save(savePath, TerminationTransactionsExcelService.fileName(result.getCompanyNumber()),
                terminationTransactionsExcelService.build(result));
    }

    public static String zipFileName(String companyNumber) {
        String trimmed = companyNumber == null ? "" : companyNumber.trim();
        return "Termination_" + trimmed + ".zip";
    }

    private void writeEntry(ZipOutputStream zip, String name, byte[] content) throws IOException {
        zip.putNextEntry(new ZipEntry(name));
        zip.write(content);
        zip.closeEntry();
    }
}
