package com.ywj.live.bank.provider.rpc;

import com.ywj.live.bank.interfaces.dto.PayProductDTO;
import com.ywj.live.bank.interfaces.rpc.IPayProductRpc;
import com.ywj.live.bank.provider.service.IPayProductService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.List;

@DubboService
public class PayProductRpcImpl implements IPayProductRpc {

    @Resource
    private IPayProductService payProductService;

    @Override
    public List<PayProductDTO> products(Integer type) {
        return payProductService.products(type);
    }
    
    @Override
    public PayProductDTO getByProductId(Integer productId) {
        return payProductService.getByProductId(productId);
    }
}