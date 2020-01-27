package com.example.oldguy.modules.modeler.services;

import org.flowable.ui.common.model.RemoteGroup;
import org.flowable.ui.common.model.RemoteToken;
import org.flowable.ui.common.model.RemoteUser;
import org.flowable.ui.common.service.idm.RemoteIdmService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @ClassName: MyRemoteServiceImpl
 * @Author: ren
 * @Description:
 * @CreateTIme: 2020/1/25 0025 下午 11:27
 **/
@Service
public class MyRemoteServiceImpl implements RemoteIdmService {

    private Logger LOGGER = LoggerFactory.getLogger(MyRemoteServiceImpl.class);

    @Override
    public RemoteUser authenticateUser(String username, String password) {
        LOGGER.debug("MyRemoteServiceImpl:authenticateUser");
        return null;
    }

    @Override
    public RemoteToken getToken(String tokenValue) {
        LOGGER.debug("MyRemoteServiceImpl:getToken");
        return null;
    }

    @Override
    public RemoteUser getUser(String userId) {
        LOGGER.debug("MyRemoteServiceImpl:getUser");
        return null;
    }

    @Override
    public List<RemoteUser> findUsersByNameFilter(String filter) {
        LOGGER.debug("MyRemoteServiceImpl:findUsersByNameFilter");
        return null;
    }

    @Override
    public List<RemoteUser> findUsersByGroup(String groupId) {
        LOGGER.debug("MyRemoteServiceImpl:findUsersByGroup");
        return null;
    }

    @Override
    public RemoteGroup getGroup(String groupId) {
        LOGGER.debug("MyRemoteServiceImpl:getGroup");
        return null;
    }

    @Override
    public List<RemoteGroup> findGroupsByNameFilter(String filter) {
        LOGGER.debug("MyRemoteServiceImpl:findGroupsByNameFilter");
        return null;
    }
}
