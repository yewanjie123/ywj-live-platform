package com.ywj.live.api.controller;


import com.ywj.live.api.service.IGiftService;
import com.ywj.live.api.vo.GiftConfigVO;
import com.ywj.live.api.vo.GiftReqVO;
import com.ywj.live.common.vo.WebResponseVO;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/gift")
public class GiftController {

    @Resource
    private IGiftService giftService;

    /**
     * 获取礼物列表
     * @return
     */
    @PostMapping("/listGift")
    public WebResponseVO listGift() {
        //调用rpc的方法，检索出来礼物配置列表
        List<GiftConfigVO> giftConfigVOS = giftService.listGift();
        return WebResponseVO.success(giftConfigVOS);
    }

    /**
     * 发送礼物方法
     * 具体实现在后边的章节会深入讲解
     * @return
     */
    @PostMapping("/send")
    public WebResponseVO send(GiftReqVO giftReqVO) {
        return WebResponseVO.success(giftService.send(giftReqVO));
    }

}