/*
 * Copyright 1999-2004 Alibaba.com All right reserved. This software is the confidential and proprietary information of
 * Alibaba.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with Alibaba.com.
 */
package com.helijia.framework.eswitch.threshold;

import java.util.concurrent.CountDownLatch;

import com.helijia.framework.eswitch.threshold.Threshold;

/**
 * @author jinli 2013-7-12
 */
@Threshold(item = "api3")
public class Api3 {

    public boolean stoped;

    public void test(Object lock, CountDownLatch latch) {
        try {
            synchronized (lock) {
                latch.countDown();
                while (!stoped) {
                    lock.wait();
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(Thread.currentThread().getName() + ":" + "Api3 End.");
    }

}
