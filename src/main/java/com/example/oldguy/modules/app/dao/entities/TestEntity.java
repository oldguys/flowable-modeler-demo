package com.example.oldguy.modules.app.dao.entities;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @ClassName: TestEntity
 * @Author: ren
 * @Description:
 * @CreateTIme: 2020/2/12 0012 下午 1:17
 **/
@Data
@TableName("app_test_entity")
public class TestEntity {

    @TableId(type = IdType.ID_WORKER)
    private Long id;

    private String name;

    private String value;
}
