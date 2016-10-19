/*
 * Copyright 1999-2004 Alibaba.com All right reserved. This software is the confidential and proprietary information of
 * Alibaba.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with Alibaba.com.
 */
package com.helijia.framework.eswitch;

import java.util.Collections;
import java.util.List;

import com.helijia.framework.eswitch.Item;
import com.helijia.framework.eswitch.item.InternalItem;
import com.helijia.framework.eswitch.item.SwitchRpcInvoker;

/**
 * @author jinli 2013-7-16
 */
public class NoOpSwitchRpcInvoker implements SwitchRpcInvoker {

    @Override
    public List<? extends InternalItem> listItems(String application) throws SwitchRpcInvokerException {
        return Collections.emptyList();
    }

    @Override
    public void register(String application, int port) throws SwitchRpcInvokerException {
    }

    @Override
    public void unregister(String application, int port) throws SwitchRpcInvokerException {
    }

    @Override
    public void collect(String application, List<? extends Item> items) throws SwitchRpcInvokerException {
    }

    @Override
    public void keepalive(String application, int port) throws SwitchRpcInvokerException {
    }

}
