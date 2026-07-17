package com.crypto.controller.system;

import com.crypto.service.FulleService;
import com.crypto.service.FulleService.EquityResult;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/fulle")
public class FulleController {

    private final FulleService fulleService;

    public FulleController(FulleService fulleService) {
        this.fulleService = fulleService;
    }

    /**
     * 获取所有公司列表（供下拉选择用）
     */
    @GetMapping("/companies")
    public Map<String, Object> getCompanies() {
        return Map.of("success", true, "data", fulleService.getAllCompanies());
    }

    /**
     * 查询全部股权穿透数据
     */
    @GetMapping("/equity/all")
    public Map<String, Object> equityAll(@RequestParam(required = false) Double marketValue) {
        BigDecimal mv = parseMarketValue(marketValue);
        List<EquityResult> results = fulleService.computeAllEquity(mv);
        return Map.of(
            "success", true,
            "data", results,
            "total", results.size()
        );
    }

    /**
     * 按人查询股权穿透
     */
    @GetMapping("/equity/person")
    public Map<String, Object> equityByPerson(@RequestParam String name,
                                              @RequestParam(required = false) Double marketValue) {
        BigDecimal mv = parseMarketValue(marketValue);
        List<EquityResult> results = fulleService.computeEquityByName(name, mv);
        return Map.of(
            "success", true,
            "data", results,
            "total", results.size()
        );
    }

    /**
     * 按子公司查询股权穿透
     */
    @GetMapping("/equity/company")
    public Map<String, Object> equityByCompany(@RequestParam Long companyId,
                                               @RequestParam(required = false) Double marketValue) {
        BigDecimal mv = parseMarketValue(marketValue);
        List<EquityResult> results = fulleService.computeEquityByCompany(companyId, mv);
        return Map.of(
            "success", true,
            "data", results,
            "total", results.size()
        );
    }

    private BigDecimal parseMarketValue(Double marketValue) {
        if (marketValue != null && marketValue > 0) {
            return new BigDecimal(marketValue.toString());
        }
        return null;
    }
}
