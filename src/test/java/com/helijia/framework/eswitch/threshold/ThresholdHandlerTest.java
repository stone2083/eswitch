/*
 * Copyright 1999-2004 Alibaba.com All right reserved. This software is the confidential and proprietary information of
 * Alibaba.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with Alibaba.com.
 */
package com.helijia.framework.eswitch.threshold;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.helijia.framework.eswitch.threshold.ThresholdException;
import com.helijia.framework.eswitch.threshold.ThresholdInterceptorX;
import com.helijia.framework.eswitch.threshold.ThresholdException.Type;
import com.helijia.framework.eswitch.threshold.handler.IgnoreThresholdNotFoundHandler;

/**
 * @author jinli 2013-10-10
 */
public class ThresholdHandlerTest {

    private static final ApplicationContext CTX = new ClassPathXmlApplicationContext("handler.xml");

    private HandlerApi                      api;
    private ThresholdInterceptorX           interceptor;

    @Before
    public void init() {
        api = (HandlerApi) CTX.getBean("api");
        interceptor = (ThresholdInterceptorX) CTX.getBean("thresholdInterceptor");
    }

    @Test
    public void testExceptionThresholdNotFound() {
        try {
            api.func1();
            Assert.fail();
        } catch (ThresholdException e) {
            Assert.assertEquals(Type.ThresholdNotFound, e.getType());
            Assert.assertTrue(e.getMessage().contains("com.helijia.framework.eswitch.threshold.HandlerApi"));
            Assert.assertTrue(e.getMessage().contains("func1"));
        }
    }

    @Test
    public void testIgnoreThresholdNotFound() {
        interceptor.setThresholdNotFoundHandler(new IgnoreThresholdNotFoundHandler());
        String ret = api.func1();
        Assert.assertEquals("func1", ret);
    }

    @Test
    public void testExceptionThresholdLimited() {
        try {
            api.func2();
        } catch (ThresholdException e) {
            Assert.assertEquals(Type.Reject, e.getType());
            Assert.assertTrue(e.getMessage().contains("com.helijia.framework.eswitch.threshold.HandlerApi"));
            Assert.assertTrue(e.getMessage().contains("func2"));
        }
    }

}
