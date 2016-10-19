/*
 * Copyright 1999-2004 Alibaba.com All right reserved. This software is the confidential and proprietary information of
 * Alibaba.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with Alibaba.com.
 */
package com.helijia.framework.eswitch.threshold.handler;

import org.aopalliance.intercept.MethodInvocation;

import com.helijia.framework.eswitch.threshold.ThresholdNotFoundHandler;

/**
 * 当流控项未找到时, 忽视, 直接执行业务逻辑.
 * 
 * @author jinli 2013-10-10
 */
public class IgnoreThresholdNotFoundHandler implements ThresholdNotFoundHandler {

    @Override
    public Object handleNotFound(MethodInvocation invocation) throws Throwable {
        return invocation.proceed();
    }

}
