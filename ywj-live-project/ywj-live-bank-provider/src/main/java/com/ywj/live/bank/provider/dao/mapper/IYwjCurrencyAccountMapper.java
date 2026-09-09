package com.ywj.live.bank.provider.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ywj.live.bank.provider.dao.po.YwjCurrencyAccountPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 虚拟币账户mapper
 */
@Mapper
public interface IYwjCurrencyAccountMapper extends BaseMapper<YwjCurrencyAccountPO> {

    @Update("update t_ywj_currency_account set current_balance = current_balance + #{num} where user_id = #{userId}")
    void incr(@Param("userId") long userId,@Param("num") int num);

    @Select("select current_balance from t_ywj_currency_account where user_id=#{userId} and status = 1 limit 1")
    Integer queryBalance(@Param("userId") long userId);

    @Update("update t_ywj_currency_account set current_balance = current_balance - #{num} where user_id = #{userId}")
    void decr(@Param("userId") long userId,@Param("num") int num);
    
}