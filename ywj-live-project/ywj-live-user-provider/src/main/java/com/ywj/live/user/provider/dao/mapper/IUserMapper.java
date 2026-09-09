package com.ywj.live.user.provider.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ywj.live.user.provider.dao.pojo.UserPO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IUserMapper extends BaseMapper<UserPO> {
}