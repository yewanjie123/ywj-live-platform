package com.ywj.live.api.service.impl;


import com.ywj.im.constants.AppIdEnum;
import com.ywj.im.interfaces.ImTokenRpc;
import com.ywj.live.api.service.ImService;
import com.ywj.live.api.vo.ImConfigVO;
import com.ywj.live.web.context.YwjRequestContext;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class ImServiceImpl implements ImService {

    @DubboReference
    private ImTokenRpc imTokenRpc;
    @Resource
    private DiscoveryClient discoveryClient;

    @Override
    public ImConfigVO getImConfig() {
        ImConfigVO imConfigVO = new ImConfigVO();
        imConfigVO.setToken(imTokenRpc.createImLoginToken(YwjRequestContext.getUserId(), AppIdEnum.YWJ_LIVE_BIZ.getCode()));
        buildImServerAddress(imConfigVO);
        return imConfigVO;
    }

    private void buildImServerAddress(ImConfigVO imConfigVO) {
        List<ServiceInstance> serviceInstanceList = discoveryClient.getInstances("ywj-live-im-core-server");
        Collections.shuffle(serviceInstanceList);
        ServiceInstance aimInstance = serviceInstanceList.get(0);
        imConfigVO.setWsImServerAddress(aimInstance.getHost() + ":8086");
        imConfigVO.setTcpImServerAddress(aimInstance.getHost() + ":8085");
    }
}