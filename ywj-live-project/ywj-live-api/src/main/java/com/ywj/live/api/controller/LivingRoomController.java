package com.ywj.live.api.controller;


import com.ywj.live.api.service.ILivingRoomService;
import com.ywj.live.api.vo.LivingRoomInitVO;
import com.ywj.live.api.vo.LivingRoomReqVO;
import com.ywj.live.api.vo.OnlinePkReqVO;
import com.ywj.live.common.vo.WebResponseVO;
import com.ywj.live.web.context.YwjRequestContext;
import com.ywj.live.web.error.BizBaseErrorEnum;
import com.ywj.live.web.error.ErrorAssert;
import com.ywj.live.web.limit.RequestLimit;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/living")
public class LivingRoomController {

    @Resource
    private ILivingRoomService livingRoomService;

    // 开启直播间的接口
    @RequestLimit(limit = 1,second = 0,msg = "开播请求过于频繁，请稍后再试")
    @PostMapping("/startingLiving")
    public WebResponseVO startingLiving(Integer type) {
        ErrorAssert.isNotNull(type, BizBaseErrorEnum.PARAM_ERROR);
       /*if(type == null){
           return WebResponseVO.errorParam("请先指定直播间的类型");
       }*/
        // 开播就是往我们的直播表里面写入一条数据即可
        int roomId = livingRoomService.startingLiving(type);
        LivingRoomInitVO initVO = new LivingRoomInitVO();
        initVO.setRoomId(roomId);
        return WebResponseVO.success(initVO);
    }


    // 关闭直播间的接口
    @RequestLimit(limit = 1,second = 0,msg = "关播请求过于频繁，请稍后再试")
    @PostMapping("/closeLiving")
    public WebResponseVO closeLiving(Integer roomId) {
        ErrorAssert.isNotNull(roomId, BizBaseErrorEnum.PARAM_ERROR);
        /*if(roomId == null){
            return WebResponseVO.errorParam("请指定直播间的类型");
        }*/
        boolean closeStatus = livingRoomService.closeLiving(roomId);
        if (closeStatus) {
            return WebResponseVO.success();
        }
        return WebResponseVO.bizError("关播异常");
    }

    /**
     * 获取主播相关配置信息（只有主播才会有权限)
     *
     * @param roomId
     * @return
     */
    @PostMapping("/anchorConfig")
    public WebResponseVO anchorConfig(Integer roomId) {
        Long userId = YwjRequestContext.getUserId();
        return WebResponseVO.success(livingRoomService.anchorConfig(userId, roomId));
    }

    // 查询直播间列表
    @PostMapping("/list")
    public WebResponseVO list(LivingRoomReqVO livingRoomReqVO) {
        ErrorAssert.isTure(livingRoomReqVO != null && livingRoomReqVO.getType() != null, BizBaseErrorEnum.PARAM_ERROR);
        ErrorAssert.isTure(livingRoomReqVO.getPage() > 0 && livingRoomReqVO.getPageSize() <= 100, BizBaseErrorEnum.PARAM_ERROR);
        /*if(livingRoomReqVO == null || livingRoomReqVO.getType() == null){
            return WebResponseVO.errorParam("请先指定直播间的类型");
        }
        if(livingRoomReqVO.getPage() <= 0 || livingRoomReqVO.getPageSize() > 100){
            return WebResponseVO.errorParam("分页查询参数有误");
        }*/
        return WebResponseVO.success(livingRoomService.list(livingRoomReqVO));
    }

    /**
     * 获取指定直播间参与pk的用户信息
     * @param onlinePkReqVO
     * @return
     */
    @PostMapping("/onlinePk")
    @RequestLimit(limit = 1,second = 3)
    public WebResponseVO onlinePk(OnlinePkReqVO onlinePkReqVO) {
        ErrorAssert.isNotNull(onlinePkReqVO.getRoomId(), BizBaseErrorEnum.PARAM_ERROR);
        return WebResponseVO.success(livingRoomService.onlinePk(onlinePkReqVO));
    }

    // 初始化红包数据
    @PostMapping("/prepareRedPacket")
    @RequestLimit(limit = 1, second = 10, msg = "红包正在初始化中，请稍等")
    public WebResponseVO prepareRedPacket(LivingRoomReqVO livingRoomReqVO) {
        return WebResponseVO.success(livingRoomService.prepareRedPacket(YwjRequestContext.getUserId(),livingRoomReqVO.getRoomId()));
    }

    @PostMapping("/startRedPacket")
    @RequestLimit(limit = 1, second = 10, msg = "正在广播直播间用户，请稍等")
    public WebResponseVO startRedPacket(LivingRoomReqVO livingRoomReqVO) {
        return WebResponseVO.success(livingRoomService.startRedPacket(YwjRequestContext.getUserId(),livingRoomReqVO.getRedPacketConfigCode()));
    }
    // 领取红包
    @PostMapping("/getRedPacket")
    @RequestLimit(limit = 1, second = 1, msg = "")
    public WebResponseVO getRedPacket(LivingRoomReqVO livingRoomReqVO) {
        return WebResponseVO.success(livingRoomService.getRedPacket(YwjRequestContext.getUserId(),livingRoomReqVO.getRedPacketConfigCode()));
    }
}

