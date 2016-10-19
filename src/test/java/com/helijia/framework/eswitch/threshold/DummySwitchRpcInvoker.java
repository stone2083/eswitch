/*
 * Copyright 1999-2004 Alibaba.com All right reserved. This software is the confidential and proprietary information of
 * Alibaba.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with Alibaba.com.
 */
package com.helijia.framework.eswitch.threshold;

import java.util.ArrayList;
import java.util.List;

import com.helijia.framework.eswitch.NoOpSwitchRpcInvoker;
import com.helijia.framework.eswitch.Item.ItemOperateException;
import com.helijia.framework.eswitch.item.DefaultItem;
import com.helijia.framework.eswitch.item.InternalItem;

/**
 * @author jinli 2013-7-11
 */
public class DummySwitchRpcInvoker extends NoOpSwitchRpcInvoker {

    @Override
    public List<? extends InternalItem> listItems(String application) throws ItemOperateException {
        List<InternalItem> items = new ArrayList<InternalItem>();
        items.add(build("api", 100));
        items.add(build("api.func2", 100));
        items.add(build("api3", 3));
        items.add(build("api_group", 10));
        return items;
    }

    private InternalItem build(String name, long threshold) {
        InternalItem item = new DefaultItem();
        item.setName(name);
        item.setThreshold(threshold);
        item.setOn(true);
        return item;
    }

}
