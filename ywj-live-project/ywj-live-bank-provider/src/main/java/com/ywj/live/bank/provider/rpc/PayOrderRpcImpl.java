package com.ywj.live.bank.provider.rpc;

import com.ywj.live.bank.interfaces.dto.PayOrderDTO;
import com.ywj.live.bank.interfaces.rpc.IPayOrderRpc;
import com.ywj.live.bank.provider.dao.po.PayOrderPO;
import com.ywj.live.bank.provider.service.IPayOrderService;
import com.ywj.live.common.utils.ConvertBeanUtils;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;

@DubboService
public class PayOrderRpcImpl implements IPayOrderRpc {

    @Resource
    private IPayOrderService payOrderService;


    @Override
    public String insertOne(PayOrderDTO payOrderDTO) {
        return payOrderService.insertOne(ConvertBeanUtils.convert(payOrderDTO, PayOrderPO.class));
    }

    @Override
    public boolean updateOrderStatus(Long id, Integer status) {
        return payOrderService.updateOrderStatus(id, status);
    }

    @Override
    public boolean updateOrderStatus(String orderId, Integer status) {
        return payOrderService.updateOrderStatus(orderId, status);
    }

    @Override
    public boolean payNotify(PayOrderDTO payOrderDTO) {
        return payOrderService.payNotify(payOrderDTO);
    }
}