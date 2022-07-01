package com.work.rest;

import com.work.service.IStockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/stock")
public class StockRest {
    @Autowired
    IStockService stockService;

    @GetMapping("/list")
    public Map<String, Object> stockList(){
        return stockService.getStockList();
    }
}
