package com.ywj.live.bank.provider.service;

import com.ywj.live.bank.provider.dao.po.PayTopicPO;

public interface IPayTopicService {

    /**
     * 根据code查询
     * @param code
     * @return
     */
    PayTopicPO getByCode(Integer code);
}