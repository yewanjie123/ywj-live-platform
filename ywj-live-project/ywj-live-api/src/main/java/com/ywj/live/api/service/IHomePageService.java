package com.ywj.live.api.service;

import com.ywj.live.api.vo.HomePageVO;

public interface IHomePageService {
    /**
     * 初始化页面获取的信息
     *
     * @param userId
     * @return
     */
    HomePageVO initPage(Long userId);
}