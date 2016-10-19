/*
 * Copyright 1999-2004 Alibaba.com All right reserved. This software is the confidential and proprietary information of
 * Alibaba.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with Alibaba.com.
 */
package com.helijia.framework.eswitch.threshold;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 阀值信息<br/>
 * 注意：在某些情况下，需要对多个方法或多个类划分为一个虚拟组进行流控，此次这些Threshold标注的item和defaultValue值必须完全相同！
 * 
 * @author jinli 2013-7-10
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
public @interface Threshold {

    /**
     * 阀值对应的开关项名.
     * 
     * @return
     */
    String item();

    /**
     * <pre>
     * 阀值默认值.
     * 当异常场景下,无法获得开关项,直接使用这个默认值.
     * </pre>
     * 
     * @return
     */
    long defaultValue() default 0;

}
