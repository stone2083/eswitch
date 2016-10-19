/*
 * Copyright 1999-2004 Alibaba.com All right reserved. This software is the confidential and proprietary information of
 * Alibaba.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with Alibaba.com.
 */
package com.helijia.framework.eswitch.threshold;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.aop.framework.ReflectiveMethodInvocation;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.helijia.framework.eswitch.item.DefaultItem;
import com.helijia.framework.eswitch.item.InternalItem;
import com.helijia.framework.eswitch.item.InternalSwitchEngine;

/**
 * @author jinli 2013-7-11
 */
public class ThresholdInterceptorTest {

    private static final ApplicationContext CTX = new ClassPathXmlApplicationContext(
            "threshold.xml");

    private ThresholdInterceptor interceptor;
    private InternalSwitchEngine se;

    private Api api;
    private Api2 api2;
    private Api3 api3;
    private InterfaceApi interfaceApi;

    private GroupApi1 groupApi1;
    private GroupApi2 groupApi2;

    private Object lock;
    private CountDownLatch latch;
    private volatile Set<String> errors;
    private Object errorLock;

    @Before
    public void init() {
        interfaceApi = (InterfaceApi) CTX.getBean("interfaceApi");
        interceptor = (ThresholdInterceptor) CTX.getBean("thresholdInterceptor");
        se = (InternalSwitchEngine) CTX.getBean("switchEngine");
        se.reload();

        api = new Api();
        api2 = new Api2();
        api3 = (Api3) CTX.getBean("api3");

        groupApi1 = (GroupApi1) CTX.getBean("groupApi1");
        groupApi2 = (GroupApi2) CTX.getBean("groupApi2");

        lock = new Object();
        latch = new CountDownLatch(3);
        errors = new HashSet<String>();
        errorLock = new Object();
    }

    @Test
    public void testChildAnnotation() {
        interfaceApi.test();
    }

    @Test
    public void testGetSph() throws Exception {
        Sph sph = null;
        // api.func1 ==> 使用class级别的annotation, 并且SwitchEngine存在对应开关项
        sph = interceptor.getSph(interceptor.getThresholdAnnotation(create(api, "func1")));
        Assert.assertSame(
                interceptor.getSph(interceptor.getThresholdAnnotation(create(api, "func1"))), sph);
        Assert.assertEquals(100, ((ItemSph) sph).getThreshold());

        // api.func1 ==> 使用class级别的annotation, 并且SwitchEngine不存在对应开关项
        se.getItems().remove("api");
        sph = interceptor.getSph(interceptor.getThresholdAnnotation(create(api, "func1")));
        Assert.assertEquals(1, ((ItemSph) sph).getThreshold());
        se.getItems().put("api", build("api", 100));

        // api.func2 ==> 使用method级别的annotation, 并且SwitchEngine存在对应开关项
        sph = interceptor.getSph(interceptor.getThresholdAnnotation(create(api, "func2")));
        Assert.assertEquals(100, ((ItemSph) sph).getThreshold());

        // api.func3 ==> 使用method级别的annotation, 并且SwitchEngine不存在对应开关项
        sph = interceptor.getSph(interceptor.getThresholdAnnotation(create(api, "func3")));
        Assert.assertEquals(3, ((ItemSph) sph).getThreshold());

        // api2.func1 ==> 没有配置Annotation
        try {
            sph = interceptor.getSph(interceptor.getThresholdAnnotation(create(api2, "func1")));
            Assert.assertNull(sph);
        } catch (ThresholdException e) {
            System.out.println(e.getMessage());
            Assert.assertEquals(
                    com.helijia.framework.eswitch.threshold.ThresholdException.Type.ThresholdNotFound,
                    e.getType());
        }
    }

    @Test
    public void testInvoke() throws Exception {
        // 通过3个.
        for (int i = 0; i < se.getThreshold("api3"); i++) {
            call(i);
        }

        // 第四个被reject
        latch.await();
        Assert.assertEquals(0, errors.size());
        call(1000);
        synchronized (errorLock) {
            while (errors.isEmpty()) {
                errorLock.wait();
            }

        }
        Assert.assertTrue(errors.contains("1000"));

        synchronized (lock) {
            api3.stoped = true;
            lock.notifyAll();
        }
    }

    @Test
    public void testInvokeOff() throws Exception {
        se.getItems().get("api3").setOn(false);
        try {
            api3.test(errorLock, latch);
            Assert.fail();
        } catch (ThresholdException e) {
            Assert.assertEquals(
                    com.helijia.framework.eswitch.threshold.ThresholdException.Type.Reject,
                    e.getType());
        }
    }

    @Test
    public void testGroupInvoke() throws Exception {
        int threshold = (int) se.getThreshold("api_group");
        latch = new CountDownLatch(threshold);
        // 用一组两个API占满所有并发
        for (int i = 0; i < threshold; i += 2) {
            callGroupApi(i, groupApi1);
            callGroupApi(i + 1, groupApi2);
        }
        // 新增加的调用将被reject
        latch.await();
        Assert.assertEquals(0, errors.size());
        callGroupApi(1000, groupApi1);
        synchronized (errorLock) {
            while (errors.isEmpty()) {
                errorLock.wait();
            }

        }
        Assert.assertTrue(errors.contains("1000"));

        synchronized (lock) {
            groupApi1.stoped = true;
            groupApi2.stoped = true;
            lock.notifyAll();
        }
    }

    // build Item Info.
    private InternalItem build(String name, long threshold) {
        InternalItem item = new DefaultItem();
        item.setName(name);
        item.setThreshold(threshold);
        return item;
    }

    private DummyMethodInvocation create(Object target, String method) throws Exception {
        return DummyMethodInvocation.newMethodInvocation(target, target.getClass()
                .getMethod(method));
    }

    private static class DummyMethodInvocation extends ReflectiveMethodInvocation {

        public static final DummyMethodInvocation newMethodInvocation(Object target, Method method) {
            return new DummyMethodInvocation(null, target, method, null, null, null);
        }

        @SuppressWarnings("rawtypes")
        protected DummyMethodInvocation(Object proxy, Object target, Method method,
                Object[] arguments, Class targetClass,
                List<Object> interceptorsAndDynamicMethodMatchers) {
            super(proxy, target, method, arguments, targetClass, interceptorsAndDynamicMethodMatchers);
        }

    }

    private void call(int id) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    api3.test(lock, latch);
                } catch (ThresholdException e) {
                    System.out.println(e.getMessage());
                    errors.add(Thread.currentThread().getName());
                    synchronized (errorLock) {
                        errorLock.notifyAll();
                    }
                }

            }
        }, String.valueOf(id)).start();
    }

    private void callGroupApi(int id, final BasicGroupApi groupApi) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    groupApi.test(lock, latch);
                } catch (ThresholdException e) {
                    System.out.println(e.getMessage());
                    errors.add(Thread.currentThread().getName());
                    synchronized (errorLock) {
                        errorLock.notifyAll();
                    }
                }

            }
        }, String.valueOf(id)).start();
    }
}
