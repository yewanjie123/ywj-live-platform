package com.ywj.live.generator.provider.rpc;

import com.ywj.live.generator.interfaces.IdGeneratorRpc;
import com.ywj.live.generator.provider.service.IdGeneratorService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;

@DubboService
public class IdGeneratorRpcImpl implements IdGeneratorRpc {

    @Resource
    private IdGeneratorService idGeneratorService;

    @Override
    public Long getSeqId(Integer id) {
        return idGeneratorService.getSeqId(id);
    }

    @Override
    public Long getUnSeqId(Integer id) {
        return idGeneratorService.getUnSeqId(id);
    }
}