package com.ywj.live.bank.api.service.impl;

import com.alibaba.fastjson2.JSON;
import com.ywj.live.bank.api.service.IPayNotifyService;
import com.ywj.live.bank.api.vo.WxPayNotifyVO;
import com.ywj.live.bank.interfaces.dto.PayOrderDTO;
import com.ywj.live.bank.interfaces.rpc.IPayOrderRpc;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

@Service
public class PayNotifyServiceImpl implements IPayNotifyService {

    @DubboReference
    private IPayOrderRpc payOrderRpc;

    @Override
    public String notifyHandler(String paramJson) {
        WxPayNotifyVO wxPayNotifyVO = JSON.parseObject(paramJson, WxPayNotifyVO.class);
        PayOrderDTO payOrderDTO = new PayOrderDTO();
        payOrderDTO.setUserId(wxPayNotifyVO.getUserId());
        payOrderDTO.setBizCode(wxPayNotifyVO.getBizCode());
        payOrderDTO.setOrderId(wxPayNotifyVO.getOrderId());
        return payOrderRpc.payNotify(payOrderDTO) ? "success" : "fail";
    }
}