package com.rubix.pension.reports.service;

import com.rubix.pension.reports.dto.DailyMovementRowDto;
import com.rubix.pension.reports.sql.MovementSummaryQueries;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class DailyMovementExcelService {

    private static final List<String> HEADERS = buildHeaders();

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public DailyMovementExcelService(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<DailyMovementRowDto> fetchRows(LocalDate reportDate) {
        Integer upCount = jdbcTemplate.queryForObject(
                MovementSummaryQueries.CHECK_UNIT_PRICE_SQL,
                new MapSqlParameterSource("valuationDate", reportDate),
                Integer.class
        );
        if (upCount == null || upCount == 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "There is no unit price available for this report date. Please enter a unit price first."
            );
        }

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                MovementSummaryQueries.DAILY_MOVEMENT_SUMMARY_SQL,
                new MapSqlParameterSource("targetDate", reportDate)
        );

        List<DailyMovementRowDto> result = new ArrayList<>();
        for (Map<String, Object> r : rows) {
            DailyMovementRowDto dto = new DailyMovementRowDto();
            dto.setModifiedDate(toLocalDateStr(r.get("Modified_Date")));
            dto.setPaymentDate(toLocalDateStr(r.get("Payment_Date")));
            dto.setDescription(asString(r.get("Description")));

            double[] up = new double[10];
            for (int i = 1; i <= 10; i++) up[i - 1] = asDouble(r.get("UP" + i));
            dto.setUp(up);

            dto.setContributionEe(asDouble(r.get("SumOfContribution_EE")));
            dto.setContributionVee(asDouble(r.get("SumOfContribution_VEE")));
            dto.setContributionEr(asDouble(r.get("SumOfContribution_ER")));
            dto.setContributionTotal(asDouble(r.get("Contribution_Total")));

            dto.setWithdrawalEe(asDouble(r.get("WithdrawalEE")));
            dto.setWithdrawalVee(asDouble(r.get("WithdrawalVEE")));
            dto.setWithdrawalEr(asDouble(r.get("WithdrawalER")));
            dto.setWithdrawalTotal(asDouble(r.get("WithdrawalTotal")));

            dto.setTopUpEe(asDouble(r.get("SumOfTopUp_EE")));
            dto.setTopUpVee(asDouble(r.get("SumOfTopUp_VEE")));
            dto.setTopUpEr(asDouble(r.get("SumOfTopUp_ER")));
            dto.setTopUpTotal(asDouble(r.get("TopUp_Total")));

            dto.setImcEe(asDouble(r.get("IMCEE")));
            dto.setImcVee(asDouble(r.get("IMCVEE")));
            dto.setImcEr(asDouble(r.get("IMCER")));
            dto.setImcTotal(asDouble(r.get("IMC_Total")));

            dto.setAdminChargesEe(asDouble(r.get("AdminCharges_EE")));
            dto.setAdminChargesEr(asDouble(r.get("AdminCharges_ER")));
            dto.setAdminChargesTotal(asDouble(r.get("AdminCharges_Total")));

            dto.setContributionChargesEe(asDouble(r.get("ContributionCharges_EE")));
            dto.setContributionChargesVee(asDouble(r.get("ContributionCharges_VEE")));
            dto.setContributionChargesEr(asDouble(r.get("ContributionCharges_ER")));
            dto.setContributionChargesTotal(asDouble(r.get("ContributionCharges_Total")));

            dto.setSurrenderChargesEe(asDouble(r.get("SurrenderCharges_EE")));
            dto.setSurrenderChargesVee(asDouble(r.get("SurrenderCharges_VEE")));
            dto.setSurrenderChargesEr(asDouble(r.get("SurrenderCharges_ER")));
            dto.setSurrenderChargesTotal(asDouble(r.get("SurrenderCharges_Total")));

            dto.setTopUpChargesEe(asDouble(r.get("TopUp_Charges_EE")));
            dto.setTopUpChargesVee(asDouble(r.get("TopUp_Charges_VEE")));
            dto.setTopUpChargesEr(asDouble(r.get("TopUp_Charges_ER")));
            dto.setTopUpChargesTotal(asDouble(r.get("TopUp_Charges_Total")));

            dto.setWithdrawalChargesEe(asDouble(r.get("WithdrawalCharges_EE")));
            dto.setWithdrawalChargesVee(asDouble(r.get("WithdrawalCharges_VEE")));
            dto.setWithdrawalChargesEr(asDouble(r.get("WithdrawalCharges_ER")));
            dto.setWithdrawalChargesTotal(asDouble(r.get("WithdrawalCharges_Total")));

            double[] txEeVal = new double[10];
            double[] txVeeVal = new double[10];
            double[] txErVal = new double[10];
            double[] termErVal = new double[10];
            double[] txEeUnits = new double[10];
            double[] txVeeUnits = new double[10];
            double[] txErUnits = new double[10];
            double[] termErUnits = new double[10];
            double[] totEeUnits = new double[10];
            double[] totVeeUnits = new double[10];
            double[] totErUnits = new double[10];
            double[] totUnits = new double[10];
            double[] txTotUnits = new double[10];
            double[] txTotValue = new double[10];

            for (int i = 1; i <= 10; i++) {
                txEeVal[i - 1] = asDouble(r.get("SumOfTransactional_EE_Value_F" + i));
                txVeeVal[i - 1] = asDouble(r.get("SumOfTransactional_VEE_Value_F" + i));
                txErVal[i - 1] = asDouble(r.get("SumOfTransactional_ER_Value_F" + i));
                termErVal[i - 1] = asDouble(r.get("SumOfTerminated_ER_Value_F" + i));

                txEeUnits[i - 1] = asDouble(r.get("SumOfTransactional_EE_Units_F" + i));
                txVeeUnits[i - 1] = asDouble(r.get("SumOfTransactional_VEE_Units_F" + i));
                txErUnits[i - 1] = asDouble(r.get("SumOfTransactional_ER_Units_F" + i));
                termErUnits[i - 1] = asDouble(r.get("SumOfTerminated_ER_Units_F" + i));

                totEeUnits[i - 1] = asDouble(r.get("SumOfTotal_EE_Units_F" + i));
                totVeeUnits[i - 1] = asDouble(r.get("SumOfTotal_VEE_Units_F" + i));
                totErUnits[i - 1] = asDouble(r.get("SumOfTotal_ER_Units_F" + i));
                totUnits[i - 1] = asDouble(r.get("SumOfTotal_Units_F" + i));

                txTotUnits[i - 1] = asDouble(r.get("SumOfTransactional_Total_Units_F" + i));
                txTotValue[i - 1] = asDouble(r.get("SumOfTransactional_Total_Value_F" + i));
            }

            dto.setTxEeValue(txEeVal);
            dto.setTxVeeValue(txVeeVal);
            dto.setTxVeeValue(txVeeVal);
            dto.setTxErValue(txErVal);
            dto.setTermErValue(termErVal);

            dto.setTxEeUnits(txEeUnits);
            dto.setTxVeeUnits(txVeeUnits);
            dto.setTxErUnits(txErUnits);
            dto.setTermErUnits(termErUnits);

            dto.setTotEeUnits(totEeUnits);
            dto.setTotVeeUnits(totVeeUnits);
            dto.setTotErUnits(totErUnits);
            dto.setTotUnits(totUnits);

            dto.setTerminationEe(asDouble(r.get("Termination_EE")));
            dto.setTerminationVee(asDouble(r.get("Termination_VEE")));
            dto.setTerminationEr(asDouble(r.get("Termination_ER")));
            dto.setTerminationTotal(asDouble(r.get("Termination_Total")));

            dto.setTxTotUnits(txTotUnits);
            dto.setTxTotValue(txTotValue);

            result.add(dto);
        }
        return result;
    }

    public byte[] buildExcel(LocalDate reportDate) throws IOException {
        List<DailyMovementRowDto> rows = fetchRows(reportDate);

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("FundsMovement");

            CellStyle dateStyle = workbook.createCellStyle();
            dateStyle.setDataFormat(workbook.getCreationHelper().createDataFormat().getFormat("yyyy-mm-dd"));

            Row headerRow = sheet.createRow(0);
            for (int c = 0; c < HEADERS.size(); c++) {
                headerRow.createCell(c).setCellValue(HEADERS.get(c));
            }

            int rowIdx = 1;
            for (DailyMovementRowDto dto : rows) {
                Row r = sheet.createRow(rowIdx++);
                int c = 0;
                setCell(r.createCell(c++), dto.getModifiedDate(), dateStyle);
                setCell(r.createCell(c++), dto.getPaymentDate(), dateStyle);
                setCell(r.createCell(c++), dto.getDescription(), null);

                for (int i = 0; i < 10; i++) setCell(r.createCell(c++), dto.getUp()[i], null);

                setCell(r.createCell(c++), dto.getContributionEe(), null);
                setCell(r.createCell(c++), dto.getContributionVee(), null);
                setCell(r.createCell(c++), dto.getContributionEr(), null);
                setCell(r.createCell(c++), dto.getContributionTotal(), null);

                setCell(r.createCell(c++), dto.getTopUpEe(), null);
                setCell(r.createCell(c++), dto.getTopUpVee(), null);
                setCell(r.createCell(c++), dto.getTopUpEr(), null);
                setCell(r.createCell(c++), dto.getTopUpTotal(), null);

                setCell(r.createCell(c++), dto.getImcEe(), null);
                setCell(r.createCell(c++), dto.getImcVee(), null);
                setCell(r.createCell(c++), dto.getImcEr(), null);
                setCell(r.createCell(c++), dto.getImcTotal(), null);

                setCell(r.createCell(c++), dto.getAdminChargesEe(), null);
                setCell(r.createCell(c++), dto.getAdminChargesEr(), null);
                setCell(r.createCell(c++), dto.getAdminChargesTotal(), null);

                setCell(r.createCell(c++), dto.getContributionChargesEe(), null);
                setCell(r.createCell(c++), dto.getContributionChargesVee(), null);
                setCell(r.createCell(c++), dto.getContributionChargesEr(), null);
                setCell(r.createCell(c++), dto.getContributionChargesTotal(), null);

                setCell(r.createCell(c++), dto.getSurrenderChargesEe(), null);
                setCell(r.createCell(c++), dto.getSurrenderChargesVee(), null);
                setCell(r.createCell(c++), dto.getSurrenderChargesEr(), null);
                setCell(r.createCell(c++), dto.getSurrenderChargesTotal(), null);

                setCell(r.createCell(c++), dto.getTopUpChargesEe(), null);
                setCell(r.createCell(c++), dto.getTopUpChargesVee(), null);
                setCell(r.createCell(c++), dto.getTopUpChargesEr(), null);
                setCell(r.createCell(c++), dto.getTopUpChargesTotal(), null);

                setCell(r.createCell(c++), dto.getWithdrawalChargesEe(), null);
                setCell(r.createCell(c++), dto.getWithdrawalChargesVee(), null);
                setCell(r.createCell(c++), dto.getWithdrawalChargesEr(), null);
                setCell(r.createCell(c++), dto.getWithdrawalChargesTotal(), null);

                for (int i = 0; i < 10; i++) setCell(r.createCell(c++), dto.getTxEeValue()[i], null);
                for (int i = 0; i < 10; i++) setCell(r.createCell(c++), dto.getTxVeeValue()[i], null);
                for (int i = 0; i < 10; i++) setCell(r.createCell(c++), dto.getTxErValue()[i], null);
                for (int i = 0; i < 10; i++) setCell(r.createCell(c++), dto.getTermErValue()[i], null);

                for (int i = 0; i < 10; i++) setCell(r.createCell(c++), dto.getTxEeUnits()[i], null);
                for (int i = 0; i < 10; i++) setCell(r.createCell(c++), dto.getTxVeeUnits()[i], null);
                for (int i = 0; i < 10; i++) setCell(r.createCell(c++), dto.getTxErUnits()[i], null);
                for (int i = 0; i < 10; i++) setCell(r.createCell(c++), dto.getTermErUnits()[i], null);

                for (int i = 0; i < 10; i++) setCell(r.createCell(c++), dto.getTotEeUnits()[i], null);
                for (int i = 0; i < 10; i++) setCell(r.createCell(c++), dto.getTotVeeUnits()[i], null);
                for (int i = 0; i < 10; i++) setCell(r.createCell(c++), dto.getTotErUnits()[i], null);
                for (int i = 0; i < 10; i++) setCell(r.createCell(c++), dto.getTotUnits()[i], null);

                setCell(r.createCell(c++), dto.getTerminationEe(), null);
                setCell(r.createCell(c++), dto.getTerminationVee(), null);
                setCell(r.createCell(c++), dto.getTerminationEr(), null);
                setCell(r.createCell(c++), dto.getTerminationTotal(), null);

                for (int i = 0; i < 10; i++) setCell(r.createCell(c++), dto.getTxTotUnits()[i], null);
                for (int i = 0; i < 10; i++) setCell(r.createCell(c++), dto.getTxTotValue()[i], null);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    public static String excelFileName(LocalDate date) {
        return "Movement_" + date.getYear() + "_" + date.getMonthValue() + "_" + date.getDayOfMonth() + ".xlsx";
    }

    public static String pdfFileName(LocalDate date) {
        return "Movement_" + date.getYear() + "_" + date.getMonthValue() + "_" + date.getDayOfMonth() + ".pdf";
    }

    private static void setCell(Cell cell, Object val, CellStyle dateStyle) {
        if (val == null) return;
        if (val instanceof Number n) {
            cell.setCellValue(n.doubleValue());
        } else if (val instanceof String s) {
            if (dateStyle != null && s.matches("\\d{4}-\\d{2}-\\d{2}")) {
                cell.setCellValue(s);
                cell.setCellStyle(dateStyle);
            } else {
                cell.setCellValue(s);
            }
        }
    }

    private static double asDouble(Object v) {
        if (v instanceof Number n) return n.doubleValue();
        return 0.0;
    }

    private static String asString(Object v) {
        return v == null ? "" : v.toString();
    }

    private static String toLocalDateStr(Object v) {
        if (v == null) return "";
        if (v instanceof java.sql.Date d) return d.toLocalDate().toString();
        if (v instanceof java.sql.Timestamp ts) return ts.toLocalDateTime().toLocalDate().toString();
        if (v instanceof LocalDate ld) return ld.toString();
        String str = v.toString();
        return str.length() >= 10 ? str.substring(0, 10) : str;
    }

    private static List<String> buildHeaders() {
        List<String> list = new ArrayList<>();
        list.add("Modified_Date");
        list.add("Payment_Date");
        list.add("Description");
        for (int i = 1; i <= 10; i++) list.add("UP" + i);

        list.add("Contribution_EE");
        list.add("Contribution_VEE");
        list.add("Contribution_ER");
        list.add("Contribution_Total");

        list.add("TopUp_EE");
        list.add("TopUp_VEE");
        list.add("TopUp_ER");
        list.add("TopUp_Total");

        list.add("IMCEE");
        list.add("IMCVEE");
        list.add("IMCER");
        list.add("IMC_Total");

        list.add("AdminCharges_EE");
        list.add("AdminCharges_ER");
        list.add("AdminCharges_Total");

        list.add("ContributionCharges_EE");
        list.add("ContributionCharges_VEE");
        list.add("ContributionCharges_ER");
        list.add("ContributionCharges_Total");

        list.add("SurrenderCharges_EE");
        list.add("SurrenderCharges_VEE");
        list.add("SurrenderCharges_ER");
        list.add("SurrenderCharges_Total");

        list.add("TopUp_Charges_EE");
        list.add("TopUp_Charges_VEE");
        list.add("TopUp_Charges_ER");
        list.add("TopUp_Charges_Total");

        list.add("WithdrawalCharges_EE");
        list.add("WithdrawalCharges_VEE");
        list.add("WithdrawalCharges_ER");
        list.add("WithdrawalCharges_Total");

        for (int i = 1; i <= 10; i++) list.add("Transactional_EE_Value_F" + i);
        for (int i = 1; i <= 10; i++) list.add("Transactional_VEE_Value_F" + i);
        for (int i = 1; i <= 10; i++) list.add("Transactional_ER_Value_F" + i);
        for (int i = 1; i <= 10; i++) list.add("Terminated_ER_Value_F" + i);

        for (int i = 1; i <= 10; i++) list.add("Transactional_EE_Units_F" + i);
        for (int i = 1; i <= 10; i++) list.add("Transactional_VEE_Units_F" + i);
        for (int i = 1; i <= 10; i++) list.add("Transactional_ER_Units_F" + i);
        for (int i = 1; i <= 10; i++) list.add("Terminated_ER_Units_F" + i);

        for (int i = 1; i <= 10; i++) list.add("Total_EE_Units_F" + i);
        for (int i = 1; i <= 10; i++) list.add("Total_VEE_Units_F" + i);
        for (int i = 1; i <= 10; i++) list.add("Total_ER_Units_F" + i);
        for (int i = 1; i <= 10; i++) list.add("Total_Units_F" + i);

        list.add("Termination_EE");
        list.add("Termination_VEE");
        list.add("Termination_ER");
        list.add("Termination_Total");

        for (int i = 1; i <= 10; i++) list.add("Transactional_Total_Units_F" + i);
        for (int i = 1; i <= 10; i++) list.add("Transactional_Total_Value_F" + i);

        return list;
    }
}
