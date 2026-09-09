package com.ywj.live.api.controller;

import com.ywj.live.api.service.IBankService;
import com.ywj.live.api.vo.PayProductReqVO;
import com.ywj.live.common.vo.WebResponseVO;
import com.ywj.live.web.error.BizBaseErrorEnum;
import com.ywj.live.web.error.ErrorAssert;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/bank")
public class BankController {

    @Resource
    private IBankService bankService;

    @PostMapping("/products")
    public WebResponseVO products(Integer type) {
        ErrorAssert.isNotNull(type, BizBaseErrorEnum.PARAM_ERROR);
        return WebResponseVO.success(bankService.products(type));
    }

    @PostMapping("/payProduct")
    public WebResponseVO payProduct(PayProductReqVO payProductReqVO) {
        return WebResponseVO.success(bankService.payProduct(payProductReqVO));

    }
}