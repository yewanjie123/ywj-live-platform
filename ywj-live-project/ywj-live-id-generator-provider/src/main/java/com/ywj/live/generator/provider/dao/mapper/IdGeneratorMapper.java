package com.ywj.live.generator.provider.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ywj.live.generator.provider.dao.po.IdGeneratorPO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface IdGeneratorMapper extends BaseMapper<IdGeneratorPO> {

    @Update("update t_id_generator_config set next_threshold=next_threshold+step," +
            "current_start=current_start+step,version=version+1 where id =#{id} and version=#{version}")
    int updateNewIdCountAndVersion(@Param("id") int id, @Param("version") int version);

    @Select("select * from t_id_generator_config")
    List<IdGeneratorPO> selectAll();
}