/*
 * Copyright 1999-2004 Alibaba.com All right reserved. This software is the confidential and proprietary information of
 * Alibaba.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with Alibaba.com.
 */
package com.helijia.framework.eswitch.threshold.configuration;

import org.aopalliance.intercept.MethodInvocation;

import com.helijia.framework.eswitch.threshold.Threshold;
import com.helijia.framework.eswitch.threshold.ThresholdConfigurationStrategy;

/**
 * 基于Annotation配置的实现.
 * 
 * @author jinli 2013-9-25
 */
public class AnnotationConfigurationStrategy implements ThresholdConfigurationStrategy {

    @Override
    public ThresholdDefinition getThresholdItem(MethodInvocation invocation) {
        // 获取方法级别的Threshold配置
        Threshold threshold = invocation.getMethod().getAnnotation(Threshold.class);
        // 获取类级别的Threshold配置
        if (threshold == null) {
            threshold = invocation.getThis().getClass().getAnnotation(Threshold.class);
        }
        if (threshold == null) {
            return null;
        }
        // 构建threshold定义
        return new ThresholdDefinition(threshold.item(), threshold.defaultValue());
    }

}
