/*
 * Copyright 1999-2004 Alibaba.com All right reserved. This software is the confidential and proprietary information of
 * Alibaba.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with Alibaba.com.
 */
package com.helijia.framework.eswitch.threshold;

import com.helijia.framework.eswitch.SwitchEngine;

/**
 * 基于开关项的Semaphore
 * 
 * @author jinli 2013-7-10
 */
public class ItemSph implements Sph {

    private String       name;
    private SwitchEngine se;
    private long         defaultValue;

    private long         count;

    public ItemSph(String name, SwitchEngine se, long defaultValue){
        this.name = name;
        this.se = se;
        this.defaultValue = defaultValue;
        // 如果se不存在这个开关,则创建一个.用defaultValue这个默认值.
        se.getThreshold(name, defaultValue);
    }

    @Override
    public boolean entry() {
        synchronized (this) {
            // 新加的功能, 当开关项为关闭的时候,直接返回false.
            if (!isOn()) {
                return false;
            }

            if (count >= getThreshold()) {
                return false;
            }
            count++;
            return true;
        }
    }

    @Override
    public boolean release() {
        synchronized (this) {
            if (count <= 0) {
                return false;
            }
            count--;
            return true;
        }
    }

    public boolean isOn() {
        return se.isOn(name, true);
    }

    public long getThreshold() {
        return se.getThreshold(name, defaultValue);
    }

    public String getName() {
        return name;
    }

    public long getCount() {
        return count;
    }

}
