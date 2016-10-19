/*
 * Copyright 1999-2004 Alibaba.com All right reserved. This software is the confidential and proprietary information of
 * Alibaba.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with Alibaba.com.
 */
package com.helijia.framework.eswitch.threshold;

import org.aopalliance.intercept.MethodInvocation;

/**
 * 当被限流时, 处理限流逻辑.
 * 
 * @author jinli 2013-10-10
 */
public interface ThresholdLimitedHandler extends ThresholdHandler {

    /**
     * 处理被流控时的逻辑.
     * 
     * @param invocation 方法调用信息
     * @param sph 流控信息
     * @return
     */
    Object handleLimited(MethodInvocation invocation, ItemSph sph) throws Throwable;
}
