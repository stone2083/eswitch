/*
 * Copyright 1999-2004 Alibaba.com All right reserved. This software is the confidential and proprietary information of
 * Alibaba.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with Alibaba.com.
 */
package com.helijia.framework.eswitch.threshold;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import com.helijia.framework.eswitch.SwitchEngine;
import com.helijia.framework.eswitch.threshold.ThresholdConfigurationStrategy.ThresholdDefinition;
import com.helijia.framework.eswitch.threshold.configuration.AnnotationConfigurationStrategy;
import com.helijia.framework.eswitch.threshold.handler.ExceptionThresholdLimitedHandler;
import com.helijia.framework.eswitch.threshold.handler.ExceptionThresholdNotFoundHandler;

/**
 * <pre>
 * ThresholdInterceptor的升级版。
 * 支持从不同的配置方式初始化流控策略。
 * 目前支持：
 * 1. Annotation
 * 2. XML
 * 
 * Threshold共享模式（默认是支持共享的）：
 * 当不同方法配置了同名item的Threshold，则这些方法通用同一个Threshold配置。
 * 
 * 如果关掉了共享模式（shared=false）：
 * 就算不同方法上Threshold的item是同名的，这些方法也是独享不同的Threshold配置（当然，流控阈值变动时，会同时影响这些Threshold）。
 * </pre>
 * 
 * @author jinli 2013-9-25
 */
public class ThresholdInterceptorX implements MethodInterceptor {

    private Collection<ThresholdConfigurationStrategy> strategies = new ArrayList<ThresholdConfigurationStrategy>(5);
    private Map<Method, Sph>                           sphs       = new ConcurrentHashMap<Method, Sph>();

    private SwitchEngine                               switchEngine;

    // 兼容1.0.0中的功能, 允许Threshold共享. 当有多个Threshold配置到同一个方法的时候, 则这些方法共用同一个Threshold. 默认不共享.
    private boolean                                    shared     = true;
    private Map<String, Sph>                           sharedSphs = new ConcurrentHashMap<String, Sph>();

    private ThresholdLimitedHandler                    thresholdLimitedHandler;
    private ThresholdNotFoundHandler                   thresholdNotFoundHandler;

    {
        // 默认支持

        // strategies
        strategies.add(new AnnotationConfigurationStrategy());
        // handler
        thresholdLimitedHandler = new ExceptionThresholdLimitedHandler();
        thresholdNotFoundHandler = new ExceptionThresholdNotFoundHandler();
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Sph sph = getSph(invocation);
        // 找不到流控项, 则处理对应逻辑.
        if (sph == null) {
            return thresholdNotFoundHandler.handleNotFound(invocation);
        }

        // 执行业务方法
        if (sph.entry()) {
            try {
                return invocation.proceed();
            } finally {
                sph.release();
            }
        }
        // 被限流了, 执行限流逻辑
        else {
            return thresholdLimitedHandler.handleLimited(invocation, (ItemSph) sph);
        }
    }

    protected Sph getSph(MethodInvocation invocation) {
        Sph sph = sphs.get(invocation.getMethod());
        // double check
        if (sph == null) {
            synchronized (this) {
                sph = sphs.get(invocation.getMethod());
                if (sph != null) {
                    return sph;
                }
                // 从不同配置方式中, 获取阀值信息.
                for (ThresholdConfigurationStrategy strategy : strategies) {
                    ThresholdDefinition def = strategy.getThresholdItem(invocation);
                    if (def != null) {
                        // 处理Threshold共享逻辑.
                        if (shared) {
                            sph = sharedSphs.get(def.getItem());
                            if (null != sph) {
                                sphs.put(invocation.getMethod(), sph);
                                return sph;
                            }
                        }

                        sph = new ItemSph(def.getItem(), switchEngine, def.getThreshold());
                        sphs.put(invocation.getMethod(), sph);
                        sharedSphs.put(def.getItem(), sph);
                        break;
                    }
                }
            }
        }
        return sph;
    }

    public void setStrategies(Collection<ThresholdConfigurationStrategy> strategies) {
        if (strategies != null) {
            this.strategies.addAll(strategies);
        }
    }

    public void setSwitchEngine(SwitchEngine switchEngine) {
        this.switchEngine = switchEngine;
    }

    public void setShared(boolean shared) {
        this.shared = shared;
    }

    public void setThresholdLimitedHandler(ThresholdLimitedHandler thresholdLimitedHandler) {
        this.thresholdLimitedHandler = thresholdLimitedHandler;
    }

    public void setThresholdNotFoundHandler(ThresholdNotFoundHandler thresholdNotFoundHandler) {
        this.thresholdNotFoundHandler = thresholdNotFoundHandler;
    }

}
