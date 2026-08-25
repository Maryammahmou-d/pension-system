package com.rubix.pension.reports.service;

import com.rubix.pension.reports.dto.EmployeeBalanceReportResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class CompanyBalanceZipService {

    private final EmployeeBalanceReportService employeeBalanceReportService;
    private final EmployeeBalancePdfService employeeBalancePdfService;
    private final EmployeeBalanceExcelService employeeBalanceExcelService;

    public CompanyBalanceZipService(
            EmployeeBalanceReportService employeeBalanceReportService,
            EmployeeBalancePdfService employeeBalancePdfService,
            EmployeeBalanceExcelService employeeBalanceExcelService
    ) {
        this.employeeBalanceReportService = employeeBalanceReportService;
        this.employeeBalancePdfService = employeeBalancePdfService;
        this.employeeBalanceExcelService = employeeBalanceExcelService;
    }

    public byte[] buildPdfZip(String companyNumber, String valuationDate, boolean activeOnly) throws IOException {
        return buildZip(companyNumber, valuationDate, activeOnly, true);
    }

    public byte[] buildExcelZip(String companyNumber, String valuationDate, boolean activeOnly) throws IOException {
        return buildZip(companyNumber, valuationDate, activeOnly, false);
    }

    public static String fileName(String valuationDate, String companyNumber) {
        String ymd = valuationDate.replace("-", "");
        return "Balance_Report_" + ymd + "_" + companyNumber + ".zip";
    }

    private byte[] buildZip(
            String companyNumber,
            String valuationDate,
            boolean activeOnly,
            boolean pdf
    ) throws IOException {
        List<String> employeeNumbers =
                employeeBalanceReportService.listCompanyEmployeeNumbers(companyNumber, valuationDate, activeOnly);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             ZipOutputStream zip = new ZipOutputStream(out)) {
            int addedEntries = 0;

            for (String employeeNumber : employeeNumbers) {
                try {
                    EmployeeBalanceReportResponse report =
                            employeeBalanceReportService.generate(companyNumber, employeeNumber, valuationDate);
                    String entryName;
                    byte[] bytes;
                    if (pdf) {
                        entryName = EmployeeBalancePdfService.fileName(report.getValuationDate(), employeeNumber);
                        bytes = employeeBalancePdfService.build(report);
                    } else {
                        entryName = EmployeeBalanceExcelService.fileName(report.getValuationDate(), employeeNumber);
                        bytes = employeeBalanceExcelService.build(report);
                    }

                    zip.putNextEntry(new ZipEntry(entryName));
                    zip.write(bytes);
                    zip.closeEntry();
                    addedEntries += 1;
                } catch (ResponseStatusException ex) {
                    if (ex.getStatusCode() != HttpStatus.NOT_FOUND) {
                        throw ex;
                    }
                }
            }

            if (addedEntries == 0) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, EmployeeBalanceReportService.NO_COMPANY_DATA_MESSAGE);
            }

            zip.finish();
            return out.toByteArray();
        }
    }
}
