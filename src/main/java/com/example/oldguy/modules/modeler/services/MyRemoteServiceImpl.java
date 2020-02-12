package com.example.oldguy.modules.modeler.services;

import org.flowable.ui.common.model.RemoteGroup;
import org.flowable.ui.common.model.RemoteToken;
import org.flowable.ui.common.model.RemoteUser;
import org.flowable.ui.common.service.idm.RemoteIdmService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
        LOGGER.warn("MyRemoteServiceImpl:authenticateUser");
        return null;
    }

    @Override
    public RemoteToken getToken(String tokenValue) {
        LOGGER.warn("MyRemoteServiceImpl:getToken");
        return null;
    }

    @Override
    public RemoteUser getUser(String userId) {
        LOGGER.warn("MyRemoteServiceImpl:getUser");
        return null;
    }

    @Override
    public List<RemoteUser> findUsersByNameFilter(String filter) {
        LOGGER.warn("MyRemoteServiceImpl:findUsersByNameFilter");
        return null;
    }

    @Override
    public List<RemoteUser> findUsersByGroup(String groupId) {
        LOGGER.warn("MyRemoteServiceImpl:findUsersByGroup");
        return null;
    }

    /**
     *  org.flowable.ui.modeler.rest.app.EditorGroupsResource#getGroups(java.lang.String)
     *  url: http://localhost:8081/flowable-modeler-demo/app/rest/editor-groups

     * @param groupId
     * @return
     */
    @Override
    public RemoteGroup getGroup(String groupId) {
        LOGGER.warn("MyRemoteServiceImpl:getGroup");
        return new RemoteGroup();
    }

    /**
     *  分配用户功能
     *
     *  http://localhost:8081/flowable-modeler-demo/app/rest/editor-groups
     *  org.flowable.ui.modeler.rest.app.EditorGroupsResource#getGroups(java.lang.String)
     * @param filter
     * @return
     */
    @Override
    public List<RemoteGroup> findGroupsByNameFilter(String filter) {
        LOGGER.warn("MyRemoteServiceImpl:findGroupsByNameFilter");

        List<RemoteGroup> groups = new ArrayList<>();
        groups.add(new RemoteGroup("01","组-01"));
        groups.add(new RemoteGroup("02","组-02"));
        return groups;
    }
}
