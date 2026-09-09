package com.ywj.live.user.provider.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ywj.live.user.provider.dao.pojo.UserTagPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface IUserTagMapper extends BaseMapper<UserTagPO> {

    /**
     * 新增标签信息
     * 使用或的思路来设置标签，只能允许第一次设置成功
     *  ${fieldName} & #{tag}=0 表示原有的标签里面不包含新增的标签
     * @param userId
     * @param fieldName
     * @param tag
     * @return
     */
    @Update("update t_user_tag set ${fieldName}=${fieldName} | #{tag} where user_id=#{userId} and ${fieldName} & #{tag}=0")
    int setTag(Long userId, String fieldName, long tag);

    /**
     * 取消标签信息
     * 使用先取反在与的思路来取消标签，只能允许第一次删除成功
     *  ${fieldName} & #{tag}={tag} 表示取消的那个标签必须存在
     * @param userId
     * @param fieldName
     * @param tag
     * @return
     */
    @Update("update t_user_tag set ${fieldName}=${fieldName} &~ #{tag} where user_id=#{userId} and ${fieldName} & #{tag}=#{tag}")
    int cancelTag(Long userId, String fieldName, long tag);
}