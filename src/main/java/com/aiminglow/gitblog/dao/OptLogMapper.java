package com.aiminglow.gitblog.dao;

import com.aiminglow.gitblog.entity.OptLog;
import com.aiminglow.gitblog.entity.OptLogExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface OptLogMapper {
    long countByExample(OptLogExample example);

    int deleteByExample(OptLogExample example);

    int deleteByPrimaryKey(Long logId);

    int insert(OptLog record);

    int insertSelective(OptLog record);

    List<OptLog> selectByExample(OptLogExample example);

    OptLog selectByPrimaryKey(Long logId);

    int updateByExampleSelective(@Param("record") OptLog record, @Param("example") OptLogExample example);

    int updateByExample(@Param("record") OptLog record, @Param("example") OptLogExample example);

    int updateByPrimaryKeySelective(OptLog record);

    int updateByPrimaryKey(OptLog record);
}