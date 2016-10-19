/*
 * Copyright 1999-2004 Alibaba.com All right reserved. This software is the confidential and proprietary information of
 * Alibaba.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with Alibaba.com.
 */
package com.helijia.framework.eswitch.threshold;

import org.aopalliance.intercept.MethodInvocation;

import com.helijia.framework.eswitch.threshold.Sph;
import com.helijia.framework.eswitch.threshold.ThresholdInterceptorX;

/**
 * @author jinli 2013-9-25
 */
public class DummyThresholdInterceptorX extends ThresholdInterceptorX {

    public Sph sph;

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        sph = getSph(invocation);
        return null;
    }

}
