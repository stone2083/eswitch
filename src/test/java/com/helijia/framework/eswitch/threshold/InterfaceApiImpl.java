package com.helijia.framework.eswitch.threshold;

import java.lang.reflect.Method;

import com.helijia.framework.eswitch.threshold.Threshold;

public class InterfaceApiImpl implements InterfaceApi {

    public void test() {
        testImpl();
    }

    @Threshold(item = "test", defaultValue = 100)
    public void testImpl() {
        System.out.println(this.getClass());
    }

    public static void main(String[] args) throws NoSuchMethodException, SecurityException {
        Method method = InterfaceApiImpl.class.getMethod("test");
        Threshold annotation = method.getAnnotation(Threshold.class);
        System.out.println(annotation);

        Method method2 = InterfaceApi.class.getMethod("test");
        Threshold annotation2 = method2.getAnnotation(Threshold.class);
        System.out.println(annotation2);
    }
}
