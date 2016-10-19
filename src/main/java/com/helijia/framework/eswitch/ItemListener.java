/*
 * Copyright 1999-2004 Alibaba.com All right reserved. This software is the confidential and proprietary information of
 * Alibaba.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with Alibaba.com.
 */
package com.helijia.framework.eswitch;

/**
 * <pre>
 * 开关项事件
 * 当开关项变动,会发出ItemChange事件.
 * </pre>
 * 
 * @author jinli 2013-7-10
 */
public interface ItemListener {

    /**
     * <pre>
     * 开关项发生变动事件. 
     * TODO: 变量是否使用ItemEvent?
     * </pre>
     * 
     * @param item 变动后开关项
     */
    void onItemChanged(Item item);

}
