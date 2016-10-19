/*
 * Copyright 1999-2004 Alibaba.com All right reserved. This software is the confidential and proprietary information of
 * Alibaba.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with Alibaba.com.
 */
package com.helijia.framework.eswitch.threshold;

import java.lang.reflect.Field;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.helijia.framework.eswitch.threshold.ItemSph;
import com.helijia.framework.eswitch.threshold.Sph;

/**
 * @author jinli 2013-9-25
 */
public class ThresholdInterceptorXTest {

    private static final ApplicationContext CTX = new ClassPathXmlApplicationContext("thresholdx.xml");

    private Api                             api;
    private Api2                            api2;

    private DummyThresholdInterceptorX      threshold;

    @Before
    public void init() {
        api = (Api) CTX.getBean("api");
        api2 = (Api2) CTX.getBean("api2");
        threshold = (DummyThresholdInterceptorX) CTX.getBean("thresholdInterceptor");
    }

    @Test
    public void testAnnotation() throws Exception {
        api.func1();
        Assert.assertEquals(1, getThreshold(threshold.sph));
        Assert.assertEquals("api", getItem(threshold.sph));

        api.func2();
        Assert.assertEquals(2, getThreshold(threshold.sph));
        Assert.assertEquals("api.func2", getItem(threshold.sph));

        api.func3();
        Assert.assertEquals(3, getThreshold(threshold.sph));
        Assert.assertEquals("api.func3", getItem(threshold.sph));
    }

    @Test
    public void testXml() throws Exception {
        api2.func1();
        Assert.assertEquals(1, getThreshold(threshold.sph));
        Assert.assertEquals("api2.func1", getItem(threshold.sph));

        api2.func2();
        Assert.assertEquals(2, getThreshold(threshold.sph));
        Assert.assertEquals("api2.func2", getItem(threshold.sph));
    }

    private String getItem(Sph sph) throws Exception {
        ItemSph r = (ItemSph) sph;
        Field f = ItemSph.class.getDeclaredField("name");
        f.setAccessible(true);
        return (String) f.get(r);
    }

    private long getThreshold(Sph sph) throws Exception {
        ItemSph r = (ItemSph) sph;
        Field f = ItemSph.class.getDeclaredField("defaultValue");
        f.setAccessible(true);
        return (Long) f.get(r);
    }

}
