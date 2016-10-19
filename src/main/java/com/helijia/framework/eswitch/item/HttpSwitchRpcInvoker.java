/*
 * Copyright 1999-2004 Alibaba.com All right reserved. This software is the confidential and proprietary information of
 * Alibaba.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with Alibaba.com.
 */
package com.helijia.framework.eswitch.item;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.helijia.framework.eswitch.Item;
import com.helijia.framework.eswitch.util.SwitchUtil;

/**
 * <pre>
 * 基于HTTP的SwitchRpcInvoker实现.
 * listItems  协议: http://switch.aliyun-inc.com/listItems?app=xxx
 * register   协议: http://switch.aliyun-inc.com/register?app=xxx&port=yyy
 * unregister 协议: http://switch.aliyun-inc.com/unregister?app=xxx&port=yyy
 * 
 * 考虑到HttpClient经常会一起包冲突,所以这边暂时不引入HttpClient包.
 * 通过手写UrlConnection来访问http.
 * 
 * 如果哪位朋友没忍住,引入了HttpClient,请通知我.
 * 
 * TODO: 服务端开发完成后,需要联调.
 * </pre>
 * 
 * @author jinli 2013-7-11
 */
public class HttpSwitchRpcInvoker implements SwitchRpcInvoker {

    private static final Logger LOGGER           = LoggerFactory
                                                         .getLogger(HttpSwitchRpcInvoker.class);

    private static final String DEFAULT_ENCODING = "UTF-8";
    private static final int    DEFAULT_TIMEOUT  = 10 * 1000;
    private static final int    STATUS_OK        = 200;

    private static final String LIST_ITEMS       = "/listItems.json";
    private static final String REGISTER         = "/register.json";
    private static final String KEEP_ALIVE       = "/keepalive.json";
    private static final String UNREGISTER       = "/unregister.json";
    private static final String COLLECT          = "/collect.json";

    private String              url;
    private String              encoding         = DEFAULT_ENCODING;
    private int                 timeout          = DEFAULT_TIMEOUT;

    @Override
    @SuppressWarnings("unchecked")
    public List<? extends InternalItem> listItems(String application)
            throws SwitchRpcInvokerException {
        if (application == null || application.isEmpty()) {
            return Collections.emptyList();
        }
        String url = this.url + LIST_ITEMS + "?app=" + application;
        String resp = access(url);
        Map<?, Object> result = SwitchUtil.parseObject(resp, Map.class);
        if (!((Boolean) result.get("success"))) {
            throw new SwitchRpcInvokerException("listItems fail.");
        }
        // 挺恶心的,JSON转来转去.
        List<?> items = (List<?>) ((Map<String, ?>) result.get("data")).get("items");
        return SwitchUtil.parseArray(JSONArray.toJSONString(items), DefaultItem.class);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void keepalive(String application, int port) {
        if (application == null || application.isEmpty() || port < 0) {
            throw new SwitchRpcInvokerException("IllegalArguments.");
        }
        String url = this.url + KEEP_ALIVE + "?app=" + application + "&port=" + port;
        String resp = access(url);
        Map<?, Object> result = SwitchUtil.parseObject(resp, Map.class);
        if (!((Boolean) result.get("success"))) {
            throw new SwitchRpcInvokerException("keepalive fail.");
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void register(String application, int port) {
        if (application == null || application.isEmpty() || port < 0) {
            throw new SwitchRpcInvokerException("IllegalArguments.");
        }
        String url = this.url + REGISTER + "?app=" + application + "&port=" + port;
        String resp = access(url);
        Map<?, Object> result = SwitchUtil.parseObject(resp, Map.class);
        if (!((Boolean) result.get("success"))) {
            throw new SwitchRpcInvokerException("register fail.");
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void unregister(String application, int port) {
        if (application == null || application.isEmpty() || port < 0) {
            throw new SwitchRpcInvokerException("IllegalArguments.");
        }
        String url = this.url + UNREGISTER + "?app=" + application + "&port=" + port;
        String resp = access(url);
        Map<?, Object> result = SwitchUtil.parseObject(resp, Map.class);
        if (!((Boolean) result.get("success"))) {
            throw new SwitchRpcInvokerException("register fail.");
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void collect(String application, List<? extends Item> items)
            throws SwitchRpcInvokerException {
        if (items == null || items.isEmpty()) {
            return;
        }
        String itemsStr = SwitchUtil.toJsonString(items);
        String url = this.url + COLLECT + "?app=" + application + "&items=" + itemsStr;
        String resp = access(url);
        Map<?, Object> result = SwitchUtil.parseObject(resp, Map.class);
        if (!(Boolean) result.get("success")) {
            throw new SwitchRpcInvokerException("collect fail.");
        }
    }

    /**
     * 访问Switch服务器.
     * 
     * @param url
     * @return
     * @throws SwitchRpcInvokerException
     */
    protected String access(String url) throws SwitchRpcInvokerException {
        HttpURLConnection con = null;
        try {
            con = (HttpURLConnection) new URL(url).openConnection();
            con.setConnectTimeout(timeout);
            con.setReadTimeout(timeout);
            con.connect();
            if (STATUS_OK == con.getResponseCode()) {
                InputStream in = con.getInputStream();
                ByteArrayOutputStream out = new ByteArrayOutputStream();

                byte[] buf = new byte[1024];
                while (true) {
                    int len = in.read(buf);
                    if (len == -1) {
                        break;
                    }
                    out.write(buf, 0, len);
                }

                String val = new String(out.toByteArray(), encoding);

                LOGGER.debug("HttpSwitchRpcInvoker#access. url={}, resp={}", new Object[] { url,
                        val });

                return val;
            } else {
                throw new SwitchRpcInvokerException("SwitchRpcInvoker Fail. access url: " + url);
            }
        } catch (Exception e) {
            throw new SwitchRpcInvokerException("SwitchRpcInvoker Fail. access url: " + url, e);
        } finally {
            if (con != null) {
                con.disconnect();
            }
        }
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public static void main(String[] args) {
        HttpSwitchRpcInvoker invoker = new HttpSwitchRpcInvoker();
        invoker.setUrl("http://127.0.0.1:8080/eswitch_console_war/eswitch");

        System.out.println(invoker.listItems("crm_m"));
        invoker.register("eswitch", 30001);
        invoker.unregister("eswitch", 30001);

        DefaultItem item1 = new DefaultItem();
        item1.setName("eswitch.item01");
        DefaultItem item2 = new DefaultItem();
        item2.setName("eswitch.item02");
        item2.setOn(true);
        item2.setThreshold(100);

        invoker.collect("eswitch", Arrays.asList(item1, item2));
    }

}
