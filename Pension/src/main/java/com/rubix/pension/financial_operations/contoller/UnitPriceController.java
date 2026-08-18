package com.rubix.pension.financial_operations.contoller;

import com.rubix.pension.financial_operations.dto.CreateUnitPriceRequest;
import com.rubix.pension.financial_operations.entity.UnitPrice;
import com.rubix.pension.financial_operations.service.UnitPriceService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/unit-price")
public class UnitPriceController {

    private final UnitPriceService unitPriceService;

    public UnitPriceController(UnitPriceService unitPriceService){
        this.unitPriceService=unitPriceService;
    }

    @GetMapping
    public List<UnitPrice> getAllUnitPrices() {
        return unitPriceService.getAllUnitPrices();
    }

    @PostMapping("/{userId}")
    public List<UnitPrice> addUnitPrice(@PathVariable Integer userId, @RequestBody CreateUnitPriceRequest request){
        return unitPriceService.addUnitPrice(userId,request);
    }
}
