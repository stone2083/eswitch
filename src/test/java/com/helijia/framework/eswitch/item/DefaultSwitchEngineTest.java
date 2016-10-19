/*
 * Copyright 1999-2004 Alibaba.com All right reserved. This software is the confidential and proprietary information of
 * Alibaba.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with Alibaba.com.
 */
package com.helijia.framework.eswitch.item;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.helijia.framework.eswitch.Item;
import com.helijia.framework.eswitch.Item.ItemNotFoundException;
import com.helijia.framework.eswitch.Item.ItemOperateException;
import com.helijia.framework.eswitch.ItemListener;
import com.helijia.framework.eswitch.NoOpSwitchRpcInvoker;

/**
 * @author jinli 2013-7-12
 */
public class DefaultSwitchEngineTest {

    private DefaultSwitchEngine engine;

    private DummyItemListener l1;
    private DummyItemListener l2;

    @Before
    public void init() {
        engine = new DefaultSwitchEngine();
        engine.setApplication("TestApp");
        engine.setInvoker(new DummySwitchRpcInvoker(0));

        l1 = new DummyItemListener();
        l2 = new DummyItemListener();
        engine.addListener(l1);
        engine.addListener(l2);
    }

    @Test
    public void testReload() {
        // 尝试4次失败
        engine.setInvoker(new DummySwitchRpcInvoker(4));
        engine.reload();
        Assert.assertEquals(0, engine.getItems().size());

        // 尝试3次,成功
        engine.setInvoker(new DummySwitchRpcInvoker(3));
        engine.reload();
        Assert.assertEquals(2, engine.getItems().size());
        Assert.assertEquals("i1", engine.getItem("i1").getName());
        Assert.assertFalse(engine.getItem("i1").isOn());
        Assert.assertEquals(1, engine.getItem("i1").getThreshold());
        Assert.assertEquals("i2", engine.getItem("i2").getName());
        Assert.assertTrue(engine.isOn("i2"));
        Assert.assertEquals(2, engine.getThreshold("i2"));

        // 尝试2次,成功. 第二次load
        engine.getItems().put("i0", build("i0", false, 0));
        engine.getItems().put("i1", build("i1", true, 0));
        engine.setInvoker(new DummySwitchRpcInvoker(3));
        engine.reload();
        Assert.assertEquals(3, engine.getItems().size());
        Assert.assertFalse(engine.getItem("i0").isOn());
        Assert.assertEquals(0, engine.getItem("i0").getThreshold());
        Assert.assertFalse(engine.getItem("i1").isOn());
        Assert.assertEquals(1, engine.getItem("i1").getThreshold());
        Assert.assertTrue(engine.isOn("i2"));
        Assert.assertEquals(2, engine.getThreshold("i2"));
    }

    @Test
    public void testItemListener() {
        DefaultItem item = new DefaultItem();
        item.setName("i1");
        item.setOn(true);
        item.setThreshold(100);

        engine.reload();
        engine.setItem("i1", item);
        Assert.assertTrue(l1.isResp());
        Assert.assertTrue(l1.getItem().isOn());
        Assert.assertEquals(100, l1.getItem().getThreshold());
        Assert.assertTrue(l2.isResp());
        Assert.assertTrue(l2.getItem().isOn());
        Assert.assertEquals(100, l2.getItem().getThreshold());
    }

    @Test
    public void testGetItem() {
        try {
            engine.getItem("NotFound");
            Assert.fail();
        } catch (ItemNotFoundException e) {
        }
    }

    // build Item Info.
    private InternalItem build(String name, boolean on, long threshold) {
        InternalItem item = new DefaultItem();
        item.setName(name);
        item.setOn(on);
        item.setThreshold(threshold);
        return item;
    }

    private class DummySwitchRpcInvoker extends NoOpSwitchRpcInvoker {

        private int maxCount;
        private int count;

        public DummySwitchRpcInvoker(int maxCount) {
            this.maxCount = maxCount;
        }

        @Override
        public List<? extends InternalItem> listItems(String application) throws ItemOperateException {
            count++;
            if (count < maxCount) {
                throw new SwitchRpcInvokerException("Fail.");
            }
            List<InternalItem> items = new ArrayList<InternalItem>();
            items.add(build("i1", false, 1));
            items.add(build("i2", true, 2));
            return items;

        }

    }

    private class DummyItemListener implements ItemListener {

        private boolean resp;
        private Item item;

        @Override
        public void onItemChanged(Item item) {
            this.resp = true;
            this.item = item;
        }

        public boolean isResp() {
            return resp;
        }

        public Item getItem() {
            return item;
        }

    }

}
