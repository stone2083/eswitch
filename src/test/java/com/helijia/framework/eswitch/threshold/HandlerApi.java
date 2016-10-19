/*
 * Copyright 1999-2004 Alibaba.com All right reserved. This software is the confidential and proprietary information of
 * Alibaba.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with Alibaba.com.
 */
package com.helijia.framework.eswitch.threshold;

import com.helijia.framework.eswitch.threshold.Threshold;

/**
 * @author jinli 2013-10-10
 */
public class HandlerApi {

    public String func1() {
        return "func1";
    }

    @Threshold(item = "handler.func2")
    public String func2() {
        return "func2";
    }

}
