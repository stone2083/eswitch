/*
 * Copyright 1999-2004 Alibaba.com All right reserved. This software is the confidential and proprietary information of
 * Alibaba.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with Alibaba.com.
 */
package com.helijia.framework.eswitch;

/**
 * @author jinli 2013-7-10
 */
public interface Item {

    /**
     * <pre>
     * 获得开关项名字.
     * 开关项名字是全局唯一的,用于标志开关项.
     * </pre>
     * 
     * @return 开关项名
     */
    String getName();

    /**
     * 判断 开关开启/关闭 状态.
     * 
     * @return 开启 OR 关闭
     */
    boolean isOn();

    /**
     * <pre>
     * 获得阀值
     * </pre>
     * 
     * @return 阀值
     */
    long getThreshold();

    /**
     * <pre>
     * 获得配置详情（字符串原始格式）
     * </pre>
     * 
     * @return 阀值
     */
    String getDetail();

    /**
     * <pre>
     * 获得自定义属性值
     * </pre>
     * 
     * @param key
     * @return
     */
    Object getUserAttribute(String key);

    /**
     * 设置自定义属性值，原始内容是不允许修改的，但应该允许使用方在item上保存一些额外的专用信息。
     * 
     * @param key
     * @param value
     */
    void setUserAttribute(String key, Object value);
    
    void clearUserAttribute(String key);
    
    void clearUserAttributes();

    /**
     * 异常: 开关项没找到.
     * 
     * @author jinli 2013-7-10
     */
    public static class ItemNotFoundException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        public ItemNotFoundException(String message) {
            super(message);
        }

    }

    /**
     * 异常: Item操作异常.
     * 
     * @author jinli 2013-7-10
     */
    public static class ItemOperateException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        public ItemOperateException(String message) {
            super(message);
        }

        public ItemOperateException(String message, Throwable cause) {
            super(message, cause);
        }

    }

}
