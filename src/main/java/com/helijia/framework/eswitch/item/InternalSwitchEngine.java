/*
 * Copyright 1999-2004 Alibaba.com All right reserved. This software is the confidential and proprietary information of
 * Alibaba.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with Alibaba.com.
 */
package com.helijia.framework.eswitch.item;

import java.util.Map;

import com.helijia.framework.eswitch.SwitchEngine;
import com.helijia.framework.eswitch.Item.ItemNotFoundException;

/**
 * 内部 SwitchEngine 类
 * 
 * @author jinli 2013-7-10
 */
public interface InternalSwitchEngine extends SwitchEngine {

    /**
     * 注册.
     */
    void register();

    /**
     * 注销.
     */
    void unregister();

    /**
     * <pre>
     * 重新加载所有开关项
     * 新增: 新增到SwitchEngine
     * 更新: 更新到SwitchEngine
     * 删除: SwitchEngine不会删除.否则太危险了. 应用重启后, 会同步最新的开关项.
     * </pre>
     */
    void reload();

    /**
     * 设置应用名
     * 
     * @param application
     */
    void setApplication(String application);

    /**
     * 获取所有开关项.
     * 
     * @return
     */
    Map<String, InternalItem> getItems();

    /**
     * 获取开关项.
     * 
     * @param name 开关项名
     * @return
     */
    InternalItem getItem(String name) throws ItemNotFoundException;

    /**
     * 保存开关项
     * 
     * @param name
     * @param item
     */
    void setItem(String name, InternalItem item);
}
