/*
 * Copyright 1999-2004 Alibaba.com All right reserved. This software is the confidential and proprietary information of
 * Alibaba.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with Alibaba.com.
 */
package com.helijia.framework.eswitch.item;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import com.helijia.framework.eswitch.Item;
import com.helijia.framework.eswitch.Item.ItemNotFoundException;
import com.helijia.framework.eswitch.ItemListener;
import com.helijia.framework.eswitch.item.SwitchRpcInvoker.SwitchRpcInvokerException;
import com.helijia.framework.eswitch.server.DefaultActionServer;

/**
 * <pre>
 * SwitchEngine实现.
 * Singleton, 一个应用只需要一份 SwitchEngine 实例.
 * 
 * @author jinli 2013-7-10
 */
public class DefaultSwitchEngine implements InternalSwitchEngine, InitializingBean, DisposableBean {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(DefaultSwitchEngine.class);
    private String application;
    private Map<String, InternalItem> items = new ConcurrentHashMap<String, InternalItem>();
    private List<ItemListener> listeners = new ArrayList<ItemListener>();

    private SwitchRpcInvoker invoker;
    private DefaultActionServer server;

    @Override
    public void afterPropertiesSet() throws Exception {
        // reload所有开关项
        reload();
        // 启动server
        if (server == null) {
            LOGGER.error("ActionServer is null.");
            return;
        }
        server.start();
        if (!server.isStartOk()) {
            LOGGER.error("ActionServer startup fail.");
            return;
        }
        // 注册（并且keepalive）
        register();
    }

    @Override
    public void destroy() throws Exception {
        if (server == null) {
            LOGGER.error("ActionServer is null.");
            return;
        }
        unregister();
        server.stop();
    }

    @Override
    public String getApplication() {
        return application;
    }

    @Override
    public void register() {
        if (invoker == null) {
            LOGGER.error("SwitchRpcInvoker is null.");
            return;
        }
        if (server == null) {
            LOGGER.error("SwitchEngine#register fail. ActionServer is null.");
            return;
        }
        if (!server.isStartOk()) {
            LOGGER.error("SwitchEngine#register fail. ActionServer startup fail.");
            return;
        }
        new Thread(new Runnable() {
            private int count;

            @Override
            public void run() {
                // 第一步：注册
                while (true) {
                    try {
                        invoker.register(application, server.getPort());
                        LOGGER.info("SwitchEngine#register OK. [retries={}]",
                                new Object[] { count });
                        break;
                    } catch (Exception e) {
                        LOGGER.error("SwitchEngine#register fail. [retries={}]",
                                new Object[] { count }, e);
                    }

                    count++;
                    try {
                        long sleep = (count < 5) ? 1000 * 60 : 1000 * 300;// 一分钟 OR 五分钟 重试一次
                        Thread.sleep(sleep);
                    } catch (InterruptedException e) {
                        // ignore.
                    }
                }
                // 第二步：定期发keepalive心跳
                while (true) {
                    try {
                        long sleep = 1000 * 300;// 五分钟一次
                        Thread.sleep(sleep);
                        invoker.keepalive(application, server.getPort());
                    } catch (Exception e) {
                        LOGGER.error("SwitchEngine#keepalive fail", e);
                    }
                }
            }
        }).start();
    }

    public void unregister() {
        // 需要配置invoker.
        if (invoker == null) {
            LOGGER.error("SwitchRpcInvoker is null.");
            return;
        }
        if (server == null) {
            LOGGER.error("SwitchEngine#unregister fail. ActionServer is null.");
            return;
        }
        if (!server.isStartOk()) {
            LOGGER.error("SwitchEngine#unregister fail. ActionServer startup fail.");
            return;
        }
        try {
            invoker.unregister(application, server.getPort());
        } catch (SwitchRpcInvokerException e) {
            // 不重试了.
            LOGGER.error("SwitchEngine#unregister fail.", e);
        }
    }

    @Override
    public void reload() {
        // 需要配置invoker.
        if (invoker == null) {
            LOGGER.error("SwitchRpcInvoker is null.");
            return;
        }

        // 这个方法很重要,出现异常时,重试3次.
        List<? extends InternalItem> all = null;
        for (int i = 0; i < 3; i++) {
            try {
                all = invoker.listItems(application);
                break;
            } catch (SwitchRpcInvokerException e) {
                LOGGER.error("SwitchRpcInvoker#listItems Fail. [application={}, retries={}]",
                        new Object[] { application, i });
                continue;
            }
        }
        // Fuck,最终还是失败了,日志记录,报警.
        if (all == null) {
            LOGGER.error("Fatal. SwitchEngine#reload Fail. [application={}]",
                    new Object[] { application });
            return;
        }

        for (InternalItem i : all) {
            // 更新
            if (items.containsKey(i.getName())) {
                InternalItem item = this.items.get(i.getName());
                item.setOn(i.isOn());
                item.setThreshold(i.getThreshold());
                item.setDetail(i.getDetail());
            }
            // 新增
            else {
                InternalItem item = new DefaultItem();
                item.setName(i.getName());
                item.setOn(i.isOn());
                item.setThreshold(i.getThreshold());
                item.setDetail(i.getDetail());
                items.put(item.getName(), item);
            }
            // 删除. Oh no...
            // 就算开关系统删除了开关项, SwitchEngine中不会删除,直到应用重启.
        }
    }

    @Override
    public Map<String, InternalItem> getItems() {
        return items;
    }

    public InternalItem getItem(String name) throws ItemNotFoundException {
        InternalItem item = items.get(name);
        if (item == null) {
            LOGGER.error("item named: [{}] not found.", new Object[] { name });
            throw new ItemNotFoundException("item named:[" + name + "] not found. ");
        }
        return item;
    }

    @Override
    public boolean isOn(String name) throws ItemNotFoundException {
        return getItem(name).isOn();
    }

    @Override
    public boolean isOn(String name, boolean defaultValue) {
        try {
            return isOn(name);
        } catch (ItemNotFoundException e) {
            InternalItem item = new DefaultItem();
            item.setName(name);
            item.setOn(defaultValue);
            item.setThreshold(0L);
            items.put(name, item);
            return defaultValue;
        }
    }

    @Override
    public long getThreshold(String name) throws ItemNotFoundException {
        return getItem(name).getThreshold();
    }

    @Override
    public long getThreshold(String name, long defaultValue) {
        try {
            return getThreshold(name);
        } catch (ItemNotFoundException e) {
            InternalItem item = new DefaultItem();
            item.setName(name);
            item.setOn(true);
            item.setThreshold(defaultValue);
            items.put(name, item);
            return defaultValue;
        }
    }

    @Override
    public void setItem(String name, InternalItem item) throws ItemNotFoundException {
        InternalItem existingItem = items.get(name);
        if (null == existingItem) {
            existingItem = item;
            items.put(name, existingItem);
        } else {
            existingItem.setOn(item.isOn());
            existingItem.setThreshold(item.getThreshold());
            existingItem.setDetail(item.getDetail());
            existingItem.clearUserAttributes();
        }
        notify(existingItem);
    }

    @Override
    public void setListeners(List<ItemListener> listeners) {
        this.listeners = listeners;
    }

    @Override
    public void addListener(ItemListener listener) {
        List<ItemListener> newList = new ArrayList<ItemListener>();
        newList.addAll(this.listeners);
        newList.add(listener);
        this.listeners = newList;
    }

    @Override
    public void removeListener(ItemListener listener) {
        List<ItemListener> newList = new ArrayList<ItemListener>();
        newList.addAll(this.listeners);
        newList.remove(listener);
        this.listeners = newList;
    }

    protected void notify(Item item) {
        if (listeners != null && !listeners.isEmpty()) {
            for (ItemListener l : listeners) {
                l.onItemChanged(item);
            }
        }
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public void setInvoker(SwitchRpcInvoker invoker) {
        this.invoker = invoker;
    }

    public void setServer(DefaultActionServer server) {
        this.server = server;
    }

    public static void main(String[] args) throws Exception {
        DefaultSwitchEngine switchEngine = new DefaultSwitchEngine();
        switchEngine.setApplication("TestApp");
        switchEngine.setServer(new DefaultActionServer());
        switchEngine.setInvoker(new SwitchRpcInvoker() {

            private int count;

            @Override
            public void unregister(String application, int port) throws SwitchRpcInvokerException {
                throw new SwitchRpcInvokerException("Fail");
            }

            @Override
            public void register(String application, int port) throws SwitchRpcInvokerException {
                count++;
                if (count < 5) {
                    throw new SwitchRpcInvokerException("Fail");
                }
            }

            @Override
            public List<? extends InternalItem> listItems(String application)
                    throws SwitchRpcInvokerException {
                throw new SwitchRpcInvokerException("Fail");
            }

            @Override
            public void collect(String application, List<? extends Item> items)
                    throws SwitchRpcInvokerException {
                throw new SwitchRpcInvokerException("Fail");
            }

            @Override
            public void keepalive(String application, int port) throws SwitchRpcInvokerException {
                throw new SwitchRpcInvokerException("Fail");
            }

        });
        switchEngine.afterPropertiesSet();
        Thread.sleep(1000 * 60 * 7);
        System.out.println("stop");
        switchEngine.destroy();
    }

}
