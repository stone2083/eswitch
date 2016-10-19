/*
 * Copyright 1999-2004 Alibaba.com All right reserved. This software is the confidential and proprietary information of
 * Alibaba.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with Alibaba.com.
 */
package com.helijia.framework.eswitch.threshold;

/**
 * 超过阀值上限,则触发 ThresholdException异常.
 * 
 * @author jinli 2013-7-10
 */
public class ThresholdException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private Type              type;

    public ThresholdException(Type type){
        super();
        this.type = type;
    }

    public ThresholdException(Type type, String message){
        super(message);
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public static enum Type {
        /** 达到上限,被拒绝 */
        Reject,
        /** 阀值配置没找到 */
        ThresholdNotFound;
    }

}
