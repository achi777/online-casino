package com.casino.controller;

import com.casino.dto.FinancialReportResponse;
import com.casino.service.FinancialService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/financial")
@RequiredArgsConstructor
public class FinancialController {

    private final FinancialService financialService;

    @GetMapping("/daily/{date}")
    public ResponseEntity<FinancialReportResponse> getDailyReport(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(financialService.getDailyReport(date));
    }

    @GetMapping("/monthly/{year}/{month}")
    public ResponseEntity<List<FinancialReportResponse>> getMonthlyReport(
            @PathVariable int year,
            @PathVariable int month) {
        return ResponseEntity.ok(financialService.getMonthlyReport(year, month));
    }

    @GetMapping("/range")
    public ResponseEntity<FinancialReportResponse> getDateRangeReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(financialService.getDateRangeReport(startDate, endDate));
    }

    @GetMapping("/export/csv")
    public ResponseEntity<String> exportToCSV(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<FinancialReportResponse> reports = generateReportsForRange(startDate, endDate);

        StringBuilder csv = new StringBuilder();
        csv.append("Date,Total Deposits,Total Withdrawals,Total Bets,Total Wins,GGR,Net Revenue\n");

        for (FinancialReportResponse report : reports) {
            csv.append(String.format("%s,%s,%s,%s,%s,%s,%s\n",
                    report.getDate(),
                    report.getTotalDeposits(),
                    report.getTotalWithdrawals(),
                    report.getTotalBets(),
                    report.getTotalWins(),
                    report.getGgr(),
                    report.getNetRevenue()
            ));
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", "financial_report.csv");

        return ResponseEntity.ok()
                .headers(headers)
                .body(csv.toString());
    }

    private List<FinancialReportResponse> generateReportsForRange(LocalDate startDate, LocalDate endDate) {
        return startDate.datesUntil(endDate.plusDays(1))
                .map(financialService::getDailyReport)
                .collect(Collectors.toList());
    }
}
