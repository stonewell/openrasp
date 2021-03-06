/*
 * Copyright 2017-2018 Baidu Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.baidu.openrasp.plugin.checker.policy.serverpolicy;

import com.baidu.openrasp.HookHandler;
import com.baidu.openrasp.plugin.checker.CheckParameter;
import com.baidu.openrasp.plugin.checker.policy.PolicyChecker;
import com.baidu.openrasp.plugin.info.EventInfo;
import com.baidu.openrasp.plugin.info.SecurityPolicyInfo;
import org.apache.log4j.Logger;

import java.util.LinkedList;
import java.util.List;

/**
 * @program openrasp
 * @description: 服务器基线检查基类
 * @author: anyang
 * @create: 2018/09/10 11:53
 */
public abstract class ServerPolicyChecker extends PolicyChecker {
    private static final Logger LOGGER = Logger.getLogger(HookHandler.class.getName());
    private static final String SERVER_CHECK_ERROR_LOG_CHANNEL = "server_security_check_error";
    private static final String WINDOWS_ADMIN_GROUP_ID = "S-1-5-32-544";

    @Override
    public List<EventInfo> checkParam(CheckParameter checkParameter) {
        List<EventInfo> infos = new LinkedList<EventInfo>();
        checkStartUser(infos);
        checkServer(checkParameter,infos);
        return infos;
    }

    public abstract void checkServer(CheckParameter checkParameter,List<EventInfo> infos);

    /**
     * 检测启动用户是否为系统管理员
     */
    private void checkStartUser(List<EventInfo> infos) {
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.startsWith("linux") || osName.startsWith("mac")) {
            if ("root".equals(System.getProperty("user.name"))) {
                infos.add(new SecurityPolicyInfo(SecurityPolicyInfo.Type.START_USER, "Java security baseline - should not start application server with root account", true));
            }
        } else if (osName.startsWith("windows")) {
            try {
                Class<?> ntSystemClass = Class.forName("com.sun.security.auth.module.NTSystem");
                Object ntSystemObject = ntSystemClass.newInstance();
                String[] userGroups = (String[]) ntSystemClass.getMethod("getGroupIDs").invoke(ntSystemObject);
                if (userGroups != null) {
                    for (String group : userGroups) {
                        if (group.equals(WINDOWS_ADMIN_GROUP_ID)) {
                            infos.add(new SecurityPolicyInfo(SecurityPolicyInfo.Type.START_USER, "Java security baseline - should not start application server with Administrator/system account", true));
                        }
                    }
                }
            } catch (Exception e) {
                LOGGER.error(SERVER_CHECK_ERROR_LOG_CHANNEL + " :" + e.getMessage(), e);
            }
        }
    }

}
