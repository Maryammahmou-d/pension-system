package com.rubix.pension.reports.service;

import com.rubix.pension.reports.dto.DateBalanceBlockDto;
import com.rubix.pension.reports.dto.MovementBetweenDatesBalanceResponse;
import com.rubix.pension.reports.dto.MovementBetweenDatesBalanceRowDto;
import com.rubix.pension.reports.dto.MovementBetweenDatesExtractRowDto;
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
public class MovementBetweenDatesReportService {

    private static final List<String> HEADERS = buildHeaders();
    private final NamedParameterJdbcTemplate jdbcTemplate;

    public MovementBetweenDatesReportService(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public MovementBetweenDatesBalanceResponse showBalance(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start date cannot be after end date.");
        }

        validateUnitPriceExists(startDate, "There is no unit price available for the Start Date (" + startDate + ").");
        validateUnitPriceExists(endDate, "There is no unit price available for the End Date (" + endDate + ").");

        DateBalanceBlockDto startBlock = calculateDateBalance(startDate);
        DateBalanceBlockDto endBlock = calculateDateBalance(endDate);

        MovementBetweenDatesBalanceResponse response = new MovementBetweenDatesBalanceResponse();
        response.setStartDate(startDate.toString());
        response.setEndDate(endDate.toString());
        response.setStartDateBalance(startBlock);
        response.setEndDateBalance(endBlock);

        return response;
    }

    private DateBalanceBlockDto calculateDateBalance(LocalDate targetDate) {
        double[][] units = fetchAsOfUnits(targetDate); // [0] = EE, [1] = VEE, [2] = ER (size 10 each)
        double[] unitPrices = fetchUnitPrices(targetDate);

        List<MovementBetweenDatesBalanceRowDto> rows = new ArrayList<>();
        MovementBetweenDatesBalanceRowDto totals = new MovementBetweenDatesBalanceRowDto();
        totals.setFund(0);

        for (int i = 0; i < 10; i++) {
            MovementBetweenDatesBalanceRowDto row = new MovementBetweenDatesBalanceRowDto();
            row.setFund(i + 1);

            double ee = units[0][i];
            double vee = units[1][i];
            double er = units[2][i];
            double totalUnits = ee + vee + er;

            double up = unitPrices[i];
            double fundsEe = ee * up;
            double fundsVee = vee * up;
            double fundsEr = er * up;
            double fundsTotal = totalUnits * up;

            row.setUnitsEe(ee);
            row.setUnitsVee(vee);
            row.setUnitsEr(er);
            row.setUnitsTotal(totalUnits);
            row.setUnitPrice(up);
            row.setFundsEe(fundsEe);
            row.setFundsVee(fundsVee);
            row.setFundsEr(fundsEr);
            row.setFundsTotal(fundsTotal);

            rows.add(row);

            totals.setUnitsEe(totals.getUnitsEe() + ee);
            totals.setUnitsVee(totals.getUnitsVee() + vee);
            totals.setUnitsEr(totals.getUnitsEr() + er);
            totals.setUnitsTotal(totals.getUnitsTotal() + totalUnits);
            totals.setFundsEe(totals.getFundsEe() + fundsEe);
            totals.setFundsVee(totals.getFundsVee() + fundsVee);
            totals.setFundsEr(totals.getFundsEr() + fundsEr);
            totals.setFundsTotal(totals.getFundsTotal() + fundsTotal);
        }

        DateBalanceBlockDto block = new DateBalanceBlockDto();
        block.setDate(targetDate.toString());
        block.setRows(rows);
        block.setTotals(totals);
        return block;
    }

    public List<MovementBetweenDatesExtractRowDto> fetchExtractRows(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start date cannot be after end date.");
        }

        LocalDate effectiveStartDate = startDate.plusDays(1);

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                MovementSummaryQueries.SUMMARY_MOVEMENTS_BETWEEN_DATES_SQL,
                new MapSqlParameterSource()
                        .addValue("startDate", effectiveStartDate)
                        .addValue("endDate", endDate)
        );

        List<MovementBetweenDatesExtractRowDto> result = new ArrayList<>();
        for (Map<String, Object> r : rows) {
            MovementBetweenDatesExtractRowDto dto = new MovementBetweenDatesExtractRowDto();
            dto.setModifiedDate(toLocalDateStr(r.get("modified_date")));
            dto.setPaymentDate(toLocalDateStr(r.get("payment_date")));

            double[] up = new double[10];
            double[] txEeUnits = new double[10];
            double[] txVeeUnits = new double[10];
            double[] txErUnits = new double[10];
            double[] termErUnits = new double[10];
            double[] txEeValue = new double[10];
            double[] txVeeValue = new double[10];
            double[] txErValue = new double[10];
            double[] termErValue = new double[10];
            double[] txTotUnits = new double[10];

            for (int i = 1; i <= 10; i++) {
                up[i - 1] = asDouble(r.get("up" + i));
                txEeUnits[i - 1] = asDouble(r.get("sum_tx_ee_units_f" + i));
                txVeeUnits[i - 1] = asDouble(r.get("sum_tx_vee_units_f" + i));
                txErUnits[i - 1] = asDouble(r.get("sum_tx_er_units_f" + i));
                termErUnits[i - 1] = asDouble(r.get("sum_term_er_units_f" + i));

                txEeValue[i - 1] = asDouble(r.get("sum_tx_ee_val_f" + i));
                txVeeValue[i - 1] = asDouble(r.get("sum_tx_vee_val_f" + i));
                txErValue[i - 1] = asDouble(r.get("sum_tx_er_val_f" + i));
                termErValue[i - 1] = asDouble(r.get("sum_term_er_val_f" + i));

                txTotUnits[i - 1] = asDouble(r.get("sum_tx_tot_units_f" + i));
            }

            dto.setUp(up);
            dto.setTxEeUnits(txEeUnits);
            dto.setTxVeeUnits(txVeeUnits);
            dto.setTxErUnits(txErUnits);
            dto.setTermErUnits(termErUnits);
            dto.setTxEeValue(txEeValue);
            dto.setTxVeeValue(txVeeValue);
            dto.setTxErValue(txErValue);
            dto.setTermErValue(termErValue);
            dto.setTxTotUnits(txTotUnits);

            result.add(dto);
        }
        return result;
    }

    public byte[] buildExcel(LocalDate startDate, LocalDate endDate) throws IOException {
        List<MovementBetweenDatesExtractRowDto> rows = fetchExtractRows(startDate, endDate);

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("TempMovements");

            CellStyle dateStyle = workbook.createCellStyle();
            dateStyle.setDataFormat(workbook.getCreationHelper().createDataFormat().getFormat("yyyy-mm-dd"));

            Row headerRow = sheet.createRow(0);
            for (int c = 0; c < HEADERS.size(); c++) {
                headerRow.createCell(c).setCellValue(HEADERS.get(c));
            }

            int rowIdx = 1;
            for (MovementBetweenDatesExtractRowDto dto : rows) {
                Row r = sheet.createRow(rowIdx++);
                int c = 0;

                // 1: ID
                setCell(r.createCell(c++), 0, null);
                // 2: Modified_Date
                setCell(r.createCell(c++), dto.getModifiedDate(), dateStyle);
                // 3: Payment_Date
                setCell(r.createCell(c++), dto.getPaymentDate(), dateStyle);
                // 4: Currency
                setCell(r.createCell(c++), "EGP", null);

                // 5..14: UP1..UP10
                for (int i = 0; i < 10; i++) setCell(r.createCell(c++), dto.getUp()[i], null);

                // 15..24: Transactional_EE_Value_F1..F10
                for (int i = 0; i < 10; i++) setCell(r.createCell(c++), dto.getTxEeValue()[i], null);

                // 25..34: Transactional_VEE_Value_F1..F10
                for (int i = 0; i < 10; i++) setCell(r.createCell(c++), dto.getTxVeeValue()[i], null);

                // 35..44: Transactional_ER_Value_F1..F10
                for (int i = 0; i < 10; i++) setCell(r.createCell(c++), dto.getTxErValue()[i], null);

                // 45..54: Terminated_ER_Value_F1..F10
                for (int i = 0; i < 10; i++) setCell(r.createCell(c++), dto.getTermErValue()[i], null);

                // 55: Transactional_EE_Value (0 in Access TempMovements)
                setCell(r.createCell(c++), 0, null);
                // 56: Transactional_VEE_Value
                setCell(r.createCell(c++), 0, null);
                // 57: Transactional_ER_Value
                setCell(r.createCell(c++), 0, null);
                // 58: Terminated_ER_Value
                setCell(r.createCell(c++), 0, null);
                // 59: Transactional_Total_Value
                setCell(r.createCell(c++), 0, null);

                // 60..69: Transactional_EE_Units_F1..F10
                for (int i = 0; i < 10; i++) setCell(r.createCell(c++), dto.getTxEeUnits()[i], null);

                // 70..79: Transactional_VEE_Units_F1..F10
                for (int i = 0; i < 10; i++) setCell(r.createCell(c++), dto.getTxVeeUnits()[i], null);

                // 80..89: Transactional_ER_Units_F1..F10
                for (int i = 0; i < 10; i++) setCell(r.createCell(c++), dto.getTxErUnits()[i], null);

                // 90..99: Terminated_ER_Units_F1..F10
                for (int i = 0; i < 10; i++) setCell(r.createCell(c++), dto.getTermErUnits()[i], null);

                // 100..109: Transactional_Total_Units_F1..F10
                for (int i = 0; i < 10; i++) setCell(r.createCell(c++), dto.getTxTotUnits()[i], null);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    public static String excelFileName(LocalDate startDate, LocalDate endDate) {
        return "Movements_" + startDate.getYear() + startDate.getMonthValue() + startDate.getDayOfMonth()
                + "_" + endDate.getYear() + endDate.getMonthValue() + endDate.getDayOfMonth() + ".xlsx";
    }

    private void validateUnitPriceExists(LocalDate date, String errorMessage) {
        Integer upCount = jdbcTemplate.queryForObject(
                MovementSummaryQueries.CHECK_UNIT_PRICE_SQL,
                new MapSqlParameterSource("valuationDate", date),
                Integer.class
        );
        if (upCount == null || upCount == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, errorMessage);
        }
    }

    private double[][] fetchAsOfUnits(LocalDate asOfDate) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                MovementSummaryQueries.AS_OF_UNITS_SUM_SQL,
                new MapSqlParameterSource("asOfDate", asOfDate)
        );

        double[][] res = new double[3][10];
        if (!rows.isEmpty()) {
            Map<String, Object> r = rows.get(0);
            for (int i = 1; i <= 10; i++) {
                res[0][i - 1] = asDouble(r.get("ee" + i));
                res[1][i - 1] = asDouble(r.get("vee" + i));
                res[2][i - 1] = asDouble(r.get("er" + i));
            }
        }
        return res;
    }

    private double[] fetchUnitPrices(LocalDate priceDate) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                MovementSummaryQueries.GET_UNIT_PRICE_SQL,
                new MapSqlParameterSource("priceDate", priceDate)
        );

        double[] res = new double[10];
        if (!rows.isEmpty()) {
            Map<String, Object> r = rows.get(0);
            for (int i = 1; i <= 10; i++) {
                res[i - 1] = asDouble(r.get("Fund" + i));
            }
        }
        return res;
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
        list.add("ID");
        list.add("Modified_Date");
        list.add("Payment_Date");
        list.add("Currency");

        for (int i = 1; i <= 10; i++) list.add("UP" + i);

        for (int i = 1; i <= 10; i++) list.add("Transactional_EE_Value_F" + i);
        for (int i = 1; i <= 10; i++) list.add("Transactional_VEE_Value_F" + i);
        for (int i = 1; i <= 10; i++) list.add("Transactional_ER_Value_F" + i);
        for (int i = 1; i <= 10; i++) list.add("Terminated_ER_Value_F" + i);

        list.add("Transactional_EE_Value");
        list.add("Transactional_VEE_Value");
        list.add("Transactional_ER_Value");
        list.add("Terminated_ER_Value");
        list.add("Transactional_Total_Value");

        for (int i = 1; i <= 10; i++) list.add("Transactional_EE_Units_F" + i);
        for (int i = 1; i <= 10; i++) list.add("Transactional_VEE_Units_F" + i);
        for (int i = 1; i <= 10; i++) list.add("Transactional_ER_Units_F" + i);
        for (int i = 1; i <= 10; i++) list.add("Terminated_ER_Units_F" + i);

        for (int i = 1; i <= 10; i++) list.add("Transactional_Total_Units_F" + i);

        return list;
    }
}
