/*
 * Copyright 1999-2004 Alibaba.com All right reserved. This software is the confidential and proprietary information of
 * Alibaba.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with Alibaba.com.
 */
package com.helijia.framework.eswitch;

import java.util.List;

import com.helijia.framework.eswitch.Item.ItemNotFoundException;

/**
 * <pre>
 * SwitchEngine, 用户API.
 * 
 * 1. 开关场景:
 * SwitchEngine se;
 * if(se.isOn("dubbo.xxx.service")) {
 *     //通过开关系统,判断服务是否正常;如果正常,则调用服务
 *     Result result = xxxService.doApi();
 * } else {
 *     //服务不正常,做预案, 比如本地mock
 *     Result result = mockResult();
 * }
 * 
 * 2. 阀值场景:
 * SwitchEngine se;
 * int count;
 * // 不超过阀值,进入服务调用
 * if(count < se.getThreshold("dubbo.xxx.service.threshold")) {
 *     count ++;
 *     Result result = xxxService.doApi();
 *     count --;
 * } 
 * // 超过阀值,做预案, 比如本地mock, 或者直接放弃
 * else {
 *     Result result = mockResult();
 * }
 * 当然, 你不需要写那么挫的代码, 框架会封装这个代码: 
 * &#64;see com.helijia.framework.eswitch.threshold.ItemSph
 * &#64;see com.helijia.framework.eswitch.threshold.ThresholdInterceptor
 * </pre>
 * 
 * @author jinli 2013-7-10
 */
public interface SwitchEngine {

    /**
     * 获得 SwitchEngine 所在的应用名字.
     * 
     * @return 应用名
     */
    String getApplication();

    /**
     * 根据开关项名,获取 开启/关闭 状态
     * 
     * @param name 开关项名
     * @return 开启/关闭 状态
     */
    boolean isOn(String name) throws ItemNotFoundException;

    /**
     * <pre>
     * 根据开关项名,获取 开启/关闭 状态
     * 当开关项不存在的情况下,直接使用参数中的默认值.
     * </pre>
     * 
     * @param name
     * @param defaultValue
     * @return
     */
    boolean isOn(String name, boolean defaultValue);

    /**
     * 根据开关项名,获取 阀值 : 用于type=THRESHOLD场景.
     * 
     * @param name 开关项名
     * @return 阀值
     */
    long getThreshold(String name) throws ItemNotFoundException;

    /**
     * <pre>
     * 根据开关项名,获取 阀值
     * 当开关项不存在的情况下,直接使用参数中的默认值.
     * </pre>
     * 
     * @param name
     * @param defaultValue
     * @return
     */
    long getThreshold(String name, long defaultValue);

    /**
     * 根据名称，查询某个开关项。
     * 
     * @param name
     * @return
     * @throws ItemNotFoundException
     */
    Item getItem(String name) throws ItemNotFoundException;

    /**
     * 设置一组监听者
     * 
     * @param listeners
     */
    void setListeners(List<ItemListener> listeners);

    /**
     * 添加一个监听者
     * 
     * @param listener
     */
    void addListener(ItemListener listener);

    /**
     * 移除一个监听者
     * 
     * @param listener
     */
    void removeListener(ItemListener listener);

}
