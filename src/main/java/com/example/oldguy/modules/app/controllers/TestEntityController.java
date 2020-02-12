package com.example.oldguy.modules.app.controllers;

import com.example.oldguy.common.dto.CommonRsp;
import com.example.oldguy.modules.app.dao.entities.TestEntity;
import com.example.oldguy.modules.app.dao.jpas.TestEntityMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @ClassName: TestEntityController
 * @Author: ren
 * @Description:
 * @CreateTIme: 2020/2/12 0012 下午 1:23
 **/
@Api(tags = "测试mybatis-plus 引入")
@RequestMapping("app")
@RestController
public class TestEntityController {

    @Autowired
    private TestEntityMapper testEntityMapper;

    @ApiOperation("测试新建")
    @PostMapping("test/create")
    public CommonRsp<TestEntity> testNewInstance() {
        TestEntity entity = new TestEntity();
        entity.setName("测试-" + System.currentTimeMillis());
        entity.setValue("Value-" + System.currentTimeMillis());
        testEntityMapper.insert(entity);
        return new CommonRsp<>(entity);
    }


    @ApiOperation("测试拉去列表")
    @GetMapping("test/list")
    public CommonRsp<List<TestEntity>> getList() {
        return new CommonRsp<>(testEntityMapper.selectList(null));
    }

}
