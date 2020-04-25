package com.example.oldguy.modules.app.dao.jpas;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.flowable.engine.impl.persistence.entity.HistoricActivityInstanceEntityImpl;

import java.util.List;

/**
 * @ClassName: HistoryActivityInstanceMapper
 * @Author: huangrenhao
 * @Description:
 * @CreateTime： 2020/2/28 0028 下午 12:19
 * @Version：
 **/
@Mapper
public interface HistoryActivityInstanceMapper {

    @Select("SELECT * FROM act_hi_actinst WHERE PROC_INST_ID_ = #{processInstanceId}")
    List<HistoricActivityInstanceEntityImpl> findList(@Param("processInstanceId") String processInstanceId);

    @Delete("DELETE FROM act_hi_actinst WHERE id_ = #{id}")
    int delete(@Param("id") String id);
}
