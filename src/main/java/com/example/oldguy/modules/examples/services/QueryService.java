package com.example.oldguy.modules.examples.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collection;

/**
 * @ClassName: QueryService
 * @Author: huangrenhao
 * @Description:
 * @CreateTime： 2020/3/19 0019 下午 9:43
 * @Version：
 **/
@Service
@Slf4j
public class QueryService {

    public String querySomeThing(Collection<String> arrays) {
        log.info("arrays:" + arrays);

        StringBuilder builder = new StringBuilder();

        for (String str : arrays) {
            if (!StringUtils.isEmpty(str)) {
                builder.append(str);
            }
        }
        return builder.toString();
    }
}
