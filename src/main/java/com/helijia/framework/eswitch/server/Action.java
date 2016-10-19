/*
 * Copyright 1999-2004 Alibaba.com All right reserved. This software is the confidential and proprietary information of
 * Alibaba.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with Alibaba.com.
 */
package com.helijia.framework.eswitch.server;

import java.util.Map;

import com.helijia.framework.eswitch.item.InternalSwitchEngine;

/**
 * 配置变更请求中分action的处理接口定义。
 * 
 * @author jinli 2013-7-10
 */
public interface Action {

    /**
     * 具体执行逻辑的接口。
     * 
     * @param action
     * @param data
     * @return
     */
    public ActionResult process(Map<String, String> context);

    /**
     * 绑定SwitchEngine的接口。
     * 
     * @param switchEngine
     */
    public void setSwitchEngine(InternalSwitchEngine switchEngine);

}
