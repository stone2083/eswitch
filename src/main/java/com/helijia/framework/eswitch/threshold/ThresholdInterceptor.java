/*
 * Copyright 1999-2004 Alibaba.com All right reserved. This software is the confidential and proprietary information of
 * Alibaba.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with Alibaba.com.
 */
package com.helijia.framework.eswitch.threshold;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import com.helijia.framework.eswitch.SwitchEngine;
import com.helijia.framework.eswitch.threshold.ThresholdException.Type;

/**
 * <pre>
 * version 1.0.0
 * 目前已经不再维护, @see {@link ThresholdInterceptorX}
 * </pre>
 * 
 * @author jinli 2013-7-10
 */
public class ThresholdInterceptor implements MethodInterceptor {

    private SwitchEngine     se;
    private Map<String, Sph> sphs = new ConcurrentHashMap<String, Sph>();

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Sph sph = getSph(getThresholdAnnotation(invocation));
        if (null == sph) {
            return invocation.proceed();
        }
        if (sph.entry()) {
            try {
                return invocation.proceed();
            } finally {
                sph.release();
            }
        }
        // 打印详细信息.
        if (sph instanceof ItemSph) {
            ItemSph r = (ItemSph) sph;
            throw new ThresholdException(Type.Reject, "ThresholdInterceptor:item name is: " + r.getName()
                                                      + "; count is: " + r.getCount() + "; threshold is :"
                                                      + r.getThreshold());
        }
        throw new ThresholdException(Type.Reject);

    }

    protected Sph getSph(Threshold threshold) {
        if (null == threshold) {
            return null;
        }
        Sph sph = sphs.get(threshold.item());
        if (sph == null) {
            synchronized (threshold) {
                sph = sphs.get(threshold.item());
                if (sph != null) {
                    return sph;
                }
                sph = new ItemSph(threshold.item(), se, threshold.defaultValue());
                sphs.put(threshold.item(), sph);
            }
        }
        return sph;
    }

    protected Threshold getThresholdAnnotation(MethodInvocation invocation) throws ThresholdException {
        Threshold threshold = invocation.getMethod().getAnnotation(Threshold.class);
        if (threshold != null) {
            return threshold;
        }
        threshold = invocation.getThis().getClass().getAnnotation(Threshold.class);
        if (threshold != null) {
            return threshold;
        }
        return null;
        // throw new ThresholdException(Type.ThresholdNotFound, "ThresholdInterceptor:class is: "
        // + invocation.getThis().getClass().getName() + "; method is: "
        // + invocation.getMethod().getName());
    }

    public void setSwitchEngine(SwitchEngine se) {
        this.se = se;
    }

}
