/*
 * Copyright 1999-2004 Alibaba.com All right reserved. This software is the confidential and proprietary information of
 * Alibaba.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with Alibaba.com.
 */
package com.helijia.framework.eswitch.item;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 开关项实现.
 * 
 * @author jinli 2013-7-11
 */
public class DefaultItem implements InternalItem {

    private String                       name;
    private volatile AtomicBoolean       on             = new AtomicBoolean();
    private volatile AtomicLong          threshold      = new AtomicLong();
    private volatile String              detail;
    private volatile Map<String, Object> userAttributes = new HashMap<String, Object>();

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isOn() {
        return on.get();
    }

    @Override
    public long getThreshold() {
        return threshold.get();
    }

    @Override
    public void setName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("name can't be null.");
        }
        this.name = name;
    }

    @Override
    public void setOn(boolean on) {
        this.on.set(on);
    }

    @Override
    public void setThreshold(long threshold) {
        if (threshold < 0) {
            throw new IllegalArgumentException("threshold is a negative number.");
        }
        this.threshold.set(threshold);
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    @Override
    public Object getUserAttribute(String key) {
        return userAttributes.get(key);
    }

    @Override
    public void setUserAttribute(String key, Object value) {
        userAttributes.put(key, value);
    }

    @Override
    public void clearUserAttribute(String key) {
        userAttributes.remove(key);
    }

    @Override
    public void clearUserAttributes() {
        userAttributes.clear();
    }

    @Override
    public String toString() {
        return "DefaultItem [name=" + name + ", on=" + on + ", threshold=" + threshold + "]";
    }

}
