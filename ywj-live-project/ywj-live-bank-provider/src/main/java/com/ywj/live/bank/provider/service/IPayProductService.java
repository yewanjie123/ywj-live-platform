package com.ywj.live.bank.provider.service;



import com.ywj.live.bank.interfaces.dto.PayProductDTO;

import java.util.List;


public interface IPayProductService {

    /**
     * 返回批量的商品信息
     * @param type 不同的业务场景所使用的产品
     */
    List<PayProductDTO> products(Integer type);
    
    /**
     * 根据产品id检索产品信息
     * @param productId
     * @return
     */
    PayProductDTO getByProductId(Integer productId);
}    