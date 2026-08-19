package com.rubix.pension.invoicing_top_up_operations.controller;

import com.rubix.pension.invoicing_top_up_operations.service.InvoiceService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @GetMapping
    public List<InvoiceService.InvoiceDto> list(@RequestParam(required = false) String status) {
        return invoiceService.list(status);
    }

    @GetMapping("/{invoiceNumber}")
    public InvoiceService.InvoiceDto get(@PathVariable String invoiceNumber) {
        return invoiceService.get(invoiceNumber);
    }

    @GetMapping("/{invoiceNumber}/details")
    public InvoiceService.InvoiceDetailsDto getDetails(@PathVariable String invoiceNumber) {
        return invoiceService.getDetails(invoiceNumber);
    }

    @PostMapping
    public InvoiceService.CreateInvoiceResult create(@RequestBody InvoiceService.CreateInvoiceRequest request) {
        return invoiceService.create(request);
    }

    @PostMapping("/{invoiceNumber}/settle")
    public InvoiceService.InvoiceDto settle(
            @PathVariable String invoiceNumber,
            @RequestBody InvoiceService.SettleInvoiceRequest request
    ) {
        return invoiceService.settle(invoiceNumber, request);
    }

    @PostMapping("/{invoiceNumber}/cancel")
    public InvoiceService.InvoiceDto cancel(
            @PathVariable String invoiceNumber,
            @RequestBody InvoiceService.CancelInvoiceRequest request
    ) {
        return invoiceService.cancel(invoiceNumber, request);
    }
}
