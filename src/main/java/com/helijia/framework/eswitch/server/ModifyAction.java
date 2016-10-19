package com.helijia.framework.eswitch.server;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helijia.framework.eswitch.Item.ItemNotFoundException;
import com.helijia.framework.eswitch.item.DefaultItem;
import com.helijia.framework.eswitch.item.InternalItem;
import com.helijia.framework.eswitch.util.SwitchUtil;

public class ModifyAction extends ActionSupport implements Action {

    private static final Logger logger = LoggerFactory.getLogger(ModifyAction.class);

    @Override
    public ActionResult process(Map<String, String> context) {
        ActionResult result = new ActionResult();

        String keysString = context.get("item");
        if (SwitchUtil.isEmpty(keysString)) {
            result.initErrorInfo(ActionResult.ERROR_CODE_INVALID_PARAMETER, "paramter 'keys' not exists");
            return result;
        }

        Set<String> updatedItemNames = new LinkedHashSet<String>();
        String[] keys = keysString.split(",");
        for (String key : keys) {
            try {
                String value = context.get(key);
                if (SwitchUtil.isEmpty(value)) {
                    logger.warn("no item-config found in request. key=" + key);
                    continue;
                }
                InternalItem newItem = SwitchUtil.parseObject(value, DefaultItem.class);
                switchEngine.setItem(key, newItem);
                updatedItemNames.add(key);

                logger.info("eswitch-item updated, detail:" + newItem.toString());
            } catch (ItemNotFoundException e) {
                logger.warn("no item found in switch-engine. key=" + key);
            }
        }

        if (updatedItemNames.size() > 0) {
            result.initSuccessInfo("modified item-count:" + updatedItemNames.size(), updatedItemNames);
        } else {
            result.initErrorInfo(ActionResult.ERROR_CODE_INVALID_PARAMETER, "no matched item found");
        }
        return result;
    }

    public static void main(String[] args) throws UnsupportedEncodingException {
        System.out.println(URLEncoder.encode("{on:true}", "utf8"));
    }
}
