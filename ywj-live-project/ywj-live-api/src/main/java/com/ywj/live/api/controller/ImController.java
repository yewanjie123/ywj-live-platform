package com.ywj.live.api.controller;


import com.ywj.live.api.service.ImService;
import com.ywj.live.common.vo.WebResponseVO;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/im")
public class ImController {

    @Resource
    private ImService imService;

    @PostMapping("/getImConfig")
    public WebResponseVO getImConfig() {
        return WebResponseVO.success(imService.getImConfig());
    }
}