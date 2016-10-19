/*
 * Copyright 1999-2004 Alibaba.com All right reserved. This software is the confidential and proprietary information of
 * Alibaba.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with Alibaba.com.
 */
package com.helijia.framework.eswitch.example;

import java.util.concurrent.CountDownLatch;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.helijia.framework.eswitch.threshold.ThresholdException;

/**
 * <pre>
 * 模拟对比测试
 *  C20 N10000 S1:
 *      => 12048 VS 12050  下降0.0167%
 * 
 *  C20 N10000 S5:
 *      => 56015 VS 56155  下降0.2499%
 *  
 *  C200 N10000 S1:
 *      => 12125 VS 12161  下降0.2969%
 *  
 *  C200 N10000 S5:
 *      => 56576 VS 56665  下降0.1573%
 *      
 * 空跑测试
 * N100000000
 *  => 39972毫秒
 *  => 执行一次开销: 0.39972微妙
 * </pre>
 * 
 * @author jinli 2013-7-18
 */
public class SwitchProfile {

    private static final ConfigurableApplicationContext CTX = new ClassPathXmlApplicationContext("example/eswitch.xml");
    private static final ProfileApi api = (ProfileApi) CTX.getBean("profileApi");
    private static final int LOOP = 100000000;
    private static final int N_THREAD = 200;

    private CountDownLatch endLatch;

    public static void main(String[] args) throws Exception {
        final SwitchProfile profile = new SwitchProfile(2);
        // profile.diffTest();
        profile.selfTest();
        CTX.close();
    }

    public SwitchProfile(int ntest) {
        this.endLatch = new CountDownLatch(ntest);
    }

    public void diffTest() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                doDiffTest(false);

            }
        }).start();

        new Thread(new Runnable() {

            @Override
            public void run() {
                doDiffTest(true);

            }
        }).start();
        try {
            endLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void selfTest() {
        long start, dur;
        // 预热
        for (int j = 0; j < LOOP; j++) {
            api.test1();
            api.test2();
        }
        for (int i = 0; i < 5; i++) {
            start = System.currentTimeMillis();
            for (int j = 0; j < LOOP; j++) {
                api.test1();
            }
            dur = System.currentTimeMillis() - start;
            System.out.println("test1 dur:" + dur);
        }

        for (int i = 0; i < 5; i++) {
            start = System.currentTimeMillis();
            for (int j = 0; j < LOOP; j++) {
                api.test2();
            }
            dur = System.currentTimeMillis() - start;
            System.out.println("test2 dur:" + dur);
        }
    }

    private void doDiffTest(boolean flag) {
        CountDownLatch latch = new CountDownLatch(N_THREAD);
        Worker worker = new Worker(latch, flag);
        worker.run(); // 预热
        long start = System.currentTimeMillis();
        for (int i = 0; i < N_THREAD; i++) {
            new Thread(worker).start();
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long dur = System.currentTimeMillis() - start;
        System.out.println("flag=" + flag + ", dur=" + dur);
        endLatch.countDown();
    }

    public static final class Worker implements Runnable {

        private CountDownLatch latch;
        private boolean flag;

        public Worker(CountDownLatch latch, boolean flag) {
            this.latch = latch;
            this.flag = flag;
        }

        @Override
        public void run() {
            for (int i = 0; i < LOOP; i++) {
                try {
                    if (flag) {
                        api.test1();
                    } else {
                        api.test2();
                    }
                } catch (ThresholdException e) {
                    System.out.println(e.getType());
                }
            }
            latch.countDown();
        }

    }

}
