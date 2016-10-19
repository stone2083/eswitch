/*
 * Copyright 1999-2004 Alibaba.com All right reserved. This software is the confidential and proprietary information of
 * Alibaba.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with Alibaba.com.
 */
package com.helijia.framework.eswitch.example;

import com.helijia.framework.eswitch.threshold.Threshold;

/**
 * @author jinli 2013-7-18
 */
@Threshold(item = "profile.api", defaultValue = 200)
public class ProfileApi {

    public void test1() {
        // sleep();
    }

    public void test2() {
        // sleep();
    }

    protected void sleep() {
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
        }
    }

}
