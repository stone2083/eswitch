/*
 * Copyright 1999-2004 Alibaba.com All right reserved. This software is the confidential and proprietary information of
 * Alibaba.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with Alibaba.com.
 */
package com.helijia.framework.eswitch.threshold;

import com.helijia.framework.eswitch.threshold.Threshold;

/**
 * @author jinli 2013-7-11
 */
@Threshold(item = "api", defaultValue = 1)
public class Api {

    public void func1() {
    }

    @Threshold(item = "api.func2", defaultValue = 2)
    public void func2() {
    }

    @Threshold(item = "api.func3", defaultValue = 3)
    public void func3() {
    }

}
