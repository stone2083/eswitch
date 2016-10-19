/*
 * Copyright 1999-2004 Alibaba.com All right reserved. This software is the confidential and proprietary information of
 * Alibaba.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with Alibaba.com.
 */
package com.helijia.framework.eswitch.threshold.handler;

import org.aopalliance.intercept.MethodInvocation;

import com.helijia.framework.eswitch.threshold.ThresholdException;
import com.helijia.framework.eswitch.threshold.ThresholdNotFoundHandler;
import com.helijia.framework.eswitch.threshold.ThresholdException.Type;

/**
 * 当流控项未找到时, 抛出 {@link ThresholdException} 异常.
 * 
 * @author jinli 2013-10-10
 */
public class ExceptionThresholdNotFoundHandler implements ThresholdNotFoundHandler {

    @Override
    public Object handleNotFound(MethodInvocation invocation) {
        throw new ThresholdException(Type.ThresholdNotFound, "ThresholdInterceptor:class is: "
                                                             + invocation.getMethod().getDeclaringClass().getName()
                                                             + "; method is: " + invocation.getMethod().getName());
    }

}
