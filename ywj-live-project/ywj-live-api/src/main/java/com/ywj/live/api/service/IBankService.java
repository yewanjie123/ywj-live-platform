package com.ywj.live.api.service;


import com.ywj.live.api.vo.PayProductReqVO;
import com.ywj.live.api.vo.PayProductRespVO;
import com.ywj.live.api.vo.PayProductVO;

public interface IBankService {

    /**
     * 查询相关的产品列表信息
     *
     * @param type
     * @return
     */
    PayProductVO products(Integer type);

    /**
     * 发起支付
     * @param payProductReqVO
     * @return
     */
    PayProductRespVO payProduct(PayProductReqVO payProductReqVO);
}