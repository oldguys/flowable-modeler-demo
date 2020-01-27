package com.example.oldguy.modules.modeler.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * @ClassName: MyPasswordEncoder
 * @Author: ren
 * @Description:
 * @CreateTIme: 2020/1/25 0025 下午 7:11
 **/
@Component
public class MyPasswordEncoder implements PasswordEncoder {

    private Logger LOGGER = LoggerFactory.getLogger(MyPasswordEncoder.class);

    @Override
    public String encode(CharSequence charSequence) {
        LOGGER.debug("MyPasswordEncoder:encode:" + charSequence);
        return charSequence.toString();
    }

    @Override
    public boolean matches(CharSequence frontPsw, String sourcePsw) {
        LOGGER.debug("MyPasswordEncoder:matches:" + frontPsw + "\t sourcePsw:" + sourcePsw);
        return true;
    }
}
