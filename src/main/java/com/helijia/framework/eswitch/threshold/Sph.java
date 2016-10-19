/*
 * Copyright 1999-2004 Alibaba.com All right reserved. This software is the confidential and proprietary information of
 * Alibaba.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with Alibaba.com.
 */
package com.helijia.framework.eswitch.threshold;

/**
 * @author jinli 2013-7-10
 */
public interface Sph {

    /**
     * <pre>
     * 获取资源
     * 当获取资源后,返回true;反之返回false.
     * </pre>
     * 
     * @return 是否成功获取资源.
     */
    boolean entry();

    /**
     * <pre>
     * 释放资源
     * 当释放资源后,返回true;反之返回false.
     * </pre>
     * 
     * @return 是否成功释放资源.
     */
    boolean release();

}
