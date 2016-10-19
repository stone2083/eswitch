/*
 * Copyright 1999-2004 Alibaba.com All right reserved. This software is the confidential and proprietary information of
 * Alibaba.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with Alibaba.com.
 */
package com.helijia.framework.eswitch.item;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helijia.framework.eswitch.item.SwitchRpcInvoker.SwitchRpcInvokerException;

/**
 * @author jinli 2013-7-16
 */
public class ItemCollector {

    private static final Logger    LOGGER              = LoggerFactory.getLogger(ItemCollector.class);

    private static final String    SWITCH_ITEM_COLLECT = "eswitch.collect";

    private InternalSwitchEngine   switchEngine;
    private SwitchRpcInvoker       invoker;
    private int                    interval            = 5;                                           // 默认五分钟收集一次.

    private Set<String>            collectedNames      = new HashSet<String>();
    private volatile AtomicBoolean flag                = new AtomicBoolean(false);
    private Object                 lock                = new Object();
    private Worker                 worker              = new Worker();
    private Thread                 thread;

    public void start() {
        if (flag.get()) {
            return;
        }
        collectedNames.add(SWITCH_ITEM_COLLECT); // 不同步这个开关项.
        flag.set(true);
        thread = new Thread(worker);
        thread.start();
    }

    public void stop() {
        if (!flag.get()) {
            return;
        }
        flag.set(false);
        thread.interrupt();
        synchronized (lock) {
            while (!worker.finished) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    // ignore.
                }
            }
        }
    }

    public void collect() {
        if (switchEngine == null) {
            LOGGER.error("ItemCollector#collect fail. switchEngine is null.");
            return;
        }
        if (invoker == null) {
            LOGGER.error("ItemCollector#collect fail. invoker is null.");
            return;
        }

        // 自己用上了开关,默认不收集.
        if (!switchEngine.isOn(SWITCH_ITEM_COLLECT, false)) {
            return;
        }

        List<InternalItem> items = new ArrayList<InternalItem>(switchEngine.getItems().values());
        Iterator<InternalItem> it = items.iterator();
        while (it.hasNext()) {
            InternalItem item = it.next();
            if (collectedNames.contains(item.getName())) {
                it.remove();
            }
        }
        if (items.size() > 50) {
            items = items.subList(0, 50); // 防止一次性数据太多.
        }

        try {
            invoker.collect(switchEngine.getApplication(), items);
            for (InternalItem internalItem : items) {
                collectedNames.add(internalItem.getName());
            }
        } catch (SwitchRpcInvokerException e) {
            LOGGER.error("ItemCollector#collect fail. invoker fail.", e);
        }

    }

    private final class Worker implements Runnable {

        volatile boolean finished;

        @Override
        public void run() {
            while (flag.get()) {
                try {
                    collect();
                    Thread.sleep(interval * 1000 * 60);
                } catch (InterruptedException e) {
                    continue;
                } catch (Exception e) {
                    LOGGER.error("Worker#run fail.", e);
                }
            }
            synchronized (lock) {
                finished = true;
                lock.notifyAll();
            }
        }
    }

    public void setSwitchEngine(InternalSwitchEngine switchEngine) {
        this.switchEngine = switchEngine;
    }

    public void setInvoker(SwitchRpcInvoker invoker) {
        this.invoker = invoker;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }
}
