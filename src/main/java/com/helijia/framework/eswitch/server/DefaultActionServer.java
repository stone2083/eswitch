package com.helijia.framework.eswitch.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.URI;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helijia.framework.eswitch.item.DefaultItem;
import com.helijia.framework.eswitch.item.DefaultSwitchEngine;
import com.helijia.framework.eswitch.item.InternalSwitchEngine;
import com.helijia.framework.eswitch.util.SwitchUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

/**
 * 一个简单的HTTP方式的Item变更通知接口的实现。<br/>
 * 外部系统通过发起HTTP请求即可调用此接口，URL格式如下：<br/>
 * http://ip:8888/eswitch/config?action=hb|modify|reload|&k1=v1&k2=v2<br/>
 * 要确定请求是否成功：<br/>
 * 1、需要判断HTTP状态码是否为200，如果不是200，说明调用失败；<br/>
 * 2、如果HTTP状态码为200，判断返回json内容中，success标记是否为true。<br/>
 * json格式为：{success:true|false,code:xxx,message:xxx}<br/>
 * 
 * @author stone
 */
@SuppressWarnings("restriction")
public class DefaultActionServer implements HttpHandler {

    private static final Logger              logger             = LoggerFactory.getLogger(DefaultActionServer.class);
    private static final String              URL_ENCODING       = "utf8";
    private static final String              RESPONSE_404       = "<h1>404 Not Found</h1>No url found for request";
    private static final String              RESPONSE_500       = "<h1>500 ERROR</h1>System error occured";
    private static final Map<String, Action> DEFAULT_ACTION_MAP = new LinkedHashMap<String, Action>();
    private static final int                 MAX_PORT           = 65535;

    static {
        DEFAULT_ACTION_MAP.put("modify", new ModifyAction());
        DEFAULT_ACTION_MAP.put("reload", new ReloadAction());
        DEFAULT_ACTION_MAP.put("print", new PrintAction());
    }

    private InternalSwitchEngine             switchEngine;
    private int                              port               = 30000;
    private boolean                          startOk            = false;
    private Map<String, Action>              actionMap          = DEFAULT_ACTION_MAP;

    private HttpServer                       server;

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setSwitchEngine(InternalSwitchEngine switchEngine) {
        this.switchEngine = switchEngine;
    }

    public boolean isStartOk() {
        return startOk;
    }

    public ActionResult process(Map<String, String> context) {
        ActionResult result = new ActionResult();

        if (SwitchUtil.isEmpty(context)) {
            logger.error("invalid context on Action.handle()");
            return result;
        }

        String action = context.get("action");
        if (null == action) {
            logger.error("no action found in context");
            return result;
        }

        Action processor = actionMap.get(action);
        if (null == processor) {
            result.setMessage("action not supported! action=" + action);
            return result;
        }

        result = processor.process(context);
        return result;
    }

    public synchronized boolean start() {
        if (startOk) {
            logger.error("server already started!");
            return true;
        }

        // 必要的话，重新绑定SwitchEngine实例
        if (null != switchEngine && null != actionMap) {
            for (Action action : actionMap.values()) {
                action.setSwitchEngine(switchEngine);
            }
        }

        // 启动HTTP-Server
        try {
            int realPort = selectPort();
            server = HttpServer.create(new InetSocketAddress(realPort), 0);// 设置HttpServer的端口
            server.createContext("/eswitch", DefaultActionServer.this);// 用当前类处理到/eswtich的请求
            server.setExecutor(null); // creates a default executor
            server.start();

            port = realPort;
            startOk = true;
        } catch (IOException e) {
            logger.error("Eswitch-HttpServer start failed!", e);
            startOk = false;
        }

        return startOk;
    }

    public synchronized void stop() {
        if (!startOk || null == server) {
            logger.error("server not running!");
            return;
        }

        server.stop(0);

        startOk = false;
        server = null;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        try {
            InputStream is = httpExchange.getRequestBody();
            String requestBody = SwitchUtil.toString(is, URL_ENCODING);
            URI requestURI = httpExchange.getRequestURI();
            String path = requestURI.getPath();
            String rawQuery = requestURI.getRawQuery();

            int lastIndex = path.lastIndexOf("/");
            String endPoint = path.substring(lastIndex + 1);
            if (!"config".equals(endPoint)) {
                httpExchange.sendResponseHeaders(404, RESPONSE_404.length());
                OutputStream os = httpExchange.getResponseBody();
                os.write(RESPONSE_404.getBytes());
                os.close();
                return;
            }

            Map<String, String> parameterMap = new HashMap<String, String>();
            if (!SwitchUtil.isEmpty(requestBody)) {
                parseParameters(parameterMap, requestBody);
            }
            if (!SwitchUtil.isEmpty(rawQuery)) {
                parseParameters(parameterMap, rawQuery);
            }

            ActionResult actionResult = null;
            try {
                actionResult = process(parameterMap);
            } catch (Exception e) {
                logger.error("error when invoking process()!", e);
                httpExchange.sendResponseHeaders(500, RESPONSE_500.length());
                OutputStream os = httpExchange.getResponseBody();
                os.write(RESPONSE_500.getBytes(URL_ENCODING));
                os.close();
                return;
            }

            String resultString = SwitchUtil.toJsonString(actionResult);
            httpExchange.sendResponseHeaders(200, resultString.length());
            OutputStream os = httpExchange.getResponseBody();
            os.write(resultString.getBytes(URL_ENCODING));
            os.close();
        } catch (Exception e) {
            logger.error("error when processing eswitch-http-request!", e);
        }
    }

    private int selectPort() throws IOException {
        for (int i = port; i < MAX_PORT; i++) {
            ServerSocket ss = null;
            try {
                ss = new ServerSocket(i);
                return i;
            } catch (IOException e) {
                logger.warn("detected port-in-use:" + i + ", will continue detect");
                continue;
            } finally {
                if (ss != null) {
                    try {
                        ss.close();
                    } catch (IOException e) {
                    }
                }
            }
        }
        throw new IOException("can not select port from " + port);
    }

    public static void parseParameters(Map<String, String> map, String data) throws UnsupportedEncodingException {
        String[] pairs = data.split("&");
        for (String pair : pairs) {
            String[] fields = pair.split("=");
            if (fields.length < 2) {
                continue;
            }

            String name = fields[0];
            String value = URLDecoder.decode(fields[1], URL_ENCODING);
            map.put(name, value);
        }
    }

    public static void main(String[] args) throws Exception {
        DefaultActionServer action = new DefaultActionServer();
        action.switchEngine = new DefaultSwitchEngine();
        action.switchEngine.getItems().put("a", new DefaultItem());
        action.switchEngine.getItems().put("b", new DefaultItem());
        action.switchEngine.getItems().put("c", new DefaultItem());
        action.start();
    }
}
