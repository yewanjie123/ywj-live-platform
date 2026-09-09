package com.ywj.live.gift.provider.rpc;


import com.ywj.live.gift.dto.GiftConfigDTO;
import com.ywj.live.gift.interfaces.IGiftConfigRpc;
import com.ywj.live.gift.provider.service.IGiftConfigService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;


import java.util.List;

/**
 * rpc接口实现
 */
@DubboService
public class GiftConfigRpcImpl implements IGiftConfigRpc {

    @Resource
    private IGiftConfigService giftConfigService;

    @Override
    public GiftConfigDTO getByGiftId(Integer giftId) {
        return giftConfigService.getByGiftId(giftId);
    }

    @Override
    public List<GiftConfigDTO> queryGiftList() {
        return giftConfigService.queryGiftList();
    }

    @Override
    public void insertOne(GiftConfigDTO giftConfigDTO) {
        giftConfigService.insertOne(giftConfigDTO);
    }

    @Override
    public void updateOne(GiftConfigDTO giftConfigDTO) {
        giftConfigService.updateOne(giftConfigDTO);
    }
}