/*
 * Copyright 1999-2004 Alibaba.com All right reserved. This software is the confidential and proprietary information of
 * Alibaba.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with Alibaba.com.
 */
package com.helijia.framework.eswitch.item;

import com.helijia.framework.eswitch.Item;

/**
 * 内部Item接口,包括set方法.
 * 
 * @author jinli 2013-7-11
 */
public interface InternalItem extends Item {

    /**
     * 设置开关项名字.
     * 
     * @param name 开关项名
     */
    void setName(String name);

    /**
     * 设置开关 开启/关闭 状态
     * 
     * @param on 开启/关闭 状态
     */
    void setOn(boolean on);

    /**
     * 设置阀值
     * 
     * @param threshold 阀值
     */
    void setThreshold(long threshold);
    
    /**
     * 设置详情
     * 
     * @param threshold 阀值
     */
    void setDetail(String detail);

}
