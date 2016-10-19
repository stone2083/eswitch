/*
 * Copyright 1999-2004 Alibaba.com All right reserved. This software is the confidential and proprietary information of
 * Alibaba.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with Alibaba.com.
 */
package com.helijia.framework.eswitch.threshold;

import com.helijia.framework.eswitch.threshold.Threshold;

/**
 * 共用流控配置的API具体类
 * 
 * @author stone
 */
@Threshold(item = "api_group", defaultValue = 10)
public class GroupApi2 extends BasicGroupApi {
}
