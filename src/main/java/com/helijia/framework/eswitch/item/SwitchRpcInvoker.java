/*
 * Copyright 1999-2004 Alibaba.com All right reserved. This software is the confidential and proprietary information of
 * Alibaba.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with Alibaba.com.
 */
package com.helijia.framework.eswitch.item;

import java.util.List;

import com.helijia.framework.eswitch.Item;

/**
 * <pre>
 * 开关RPC调用: 向开关系统获取数据.
 * </pre>
 * 
 * @author jinli 2013-7-11
 */
public interface SwitchRpcInvoker {

    /**
     * <pre>
     * 拿到一个application下的所有开关项.
     * </pre>
     * 
     * @param application 应用名.
     * @return 所有开关项 如果没有item,则返回空的List对象,不会返回null.
     */
    List<? extends InternalItem> listItems(String application) throws SwitchRpcInvokerException;

    /**
     * 注册
     * 
     * @param application
     * @param host
     * @param port
     */
    void register(String application, int port) throws SwitchRpcInvokerException;
    
    /**
     * 存活报告
     * 
     * @param application
     * @param host
     * @param port
     */
    void keepalive(String application, int port) throws SwitchRpcInvokerException;

    /**
     * 注销
     * 
     * @param application
     * @param host
     * @param port
     */
    void unregister(String application, int port) throws SwitchRpcInvokerException;

    /**
     * 收集应用中的所有开关项
     * 
     * @param items
     * @throws SwitchRpcInvokerException
     */
    void collect(String application, List<? extends Item> items) throws SwitchRpcInvokerException;

    /**
     * RPC异常
     * 
     * @author jinli 2013-7-15
     */
    static class SwitchRpcInvokerException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        public SwitchRpcInvokerException(String message, Throwable cause){
            super(message, cause);
        }

        public SwitchRpcInvokerException(String message){
            super(message);
        }

    }

}
