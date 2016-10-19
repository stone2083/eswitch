/*
 * Copyright 1999-2004 Alibaba.com All right reserved. This software is the confidential and proprietary information of
 * Alibaba.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with Alibaba.com.
 */
package com.helijia.framework.eswitch.threshold;

import org.aopalliance.intercept.MethodInvocation;

/**
 * <pre>
 * Threshold配置.
 * 可以实现不同的配置方案,比如:
 * 1. 基于Annotation配置;
 * 2. 基于XML配置;
 * </pre>
 * 
 * @author jinli 2013-9-25
 */
public interface ThresholdConfigurationStrategy {

    /**
     * <pre>
     * 获取方法的Threshold配置信息.
     * </pre>
     * 
     * @param clazz
     * @param method
     * @return
     */
    ThresholdDefinition getThresholdItem(MethodInvocation invocation);

    /**
     * 阀值定义类
     * 
     * @author jinli 2013-9-25
     */
    static class ThresholdDefinition {

        private String item;
        private long   threshold;

        public ThresholdDefinition(String item, long threshold){
            this.item = item;
            this.threshold = threshold;
        }

        public String getItem() {
            return item;
        }

        public void setItem(String item) {
            this.item = item;
        }

        public long getThreshold() {
            return threshold;
        }

        public void setThreshold(long threshold) {
            this.threshold = threshold;
        }

    }

}
