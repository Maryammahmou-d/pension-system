package com.rubix.pension.invoicing_top_up_operations.controller;

import com.rubix.pension.invoicing_top_up_operations.service.TopUpService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/top-ups")
public class TopUpController {

    private final TopUpService topUpService;

    public TopUpController(TopUpService topUpService) {
        this.topUpService = topUpService;
    }

    @PostMapping
    public TopUpService.TopUpResult create(@RequestBody TopUpService.TopUpRequest request) {
        return topUpService.create(request);
    }

    @PostMapping("/bulk")
    public TopUpService.BulkTopUpResult bulk(@RequestBody TopUpService.BulkTopUpRequest request) {
        return topUpService.bulk(request);
    }
}
