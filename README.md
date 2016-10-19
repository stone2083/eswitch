# eswitch
eswitch：开关系统，流控降级方案的核心组件

## 背景介绍
目前:
* 应用服务都没有流控保护,当业务突增,访问量加大引起服务的不稳定,甚至宕机,从而导致全网故障

    ==> 我们需要有一套系统, 对所有服务做流量保护, 当流量达到阀值时, 做流量控制,防止服务宕机, 牺牲局部, 保全大局;
* 应用核心业务逻辑, 比较脆弱, 往往一个依赖点(比如外部服务)的故障,导致整个核心业务异常, 没有做好服务降级工作

    ==> 我们需要有一套系统, 当依赖点故障时, 能立刻切换到预案流程, 在线做到服务降级;

ESwitch的出现,主要用于解决上述问题.

## 功能介绍
* 开关功能

    开关功能, 允许为每一个核心业务功能准备一个预案,当核心功能因不可抗拒原因无法正常进行时,通过切换开关,立刻切换到预案分支.保证核心业务的100%可用性.
* 流控功能

    流控功能, 允许为每一个服务设置调用阀值, 当调用达到阀值后, 进行流量控制, 从而保护服务的可用性, 不因为局部导致全局受损.
* 动态调整开关值
    可以动态掉正开关值(阀值), 时刻生效, 第一时间进行开关或者流控.

## 架构
<img src="https://raw.githubusercontent.com/stone2083/assets/master/eswitch/arch.png" width="640">

## 如何使用
#### 引入eswitch框架
maven pom引入eswitch依赖.
```xml
<!-- 稳定版 -->
<dependency>
    <groupId>com.helijia.framework</groupId>
    <artifactId>helijia.eswitch</artifactId>
    <version>1.0.0</version>
</dependency>
```

#### 配置&使用eswitch核心组件
```xml
<!--
开关引擎:eswitch的核心组件
开关引擎对应一个应用名, 如Test.App
开关引擎对应一个server,用于动态调整开关值(阀值)
开关引擎对应一个invoker, 用于和eswitch server进行通讯
通过start,启动开关引擎; 通过stop关闭开关引擎
-->
<bean id="switchEngine" class="com.helijia.framework.eswitch.item.DefaultSwitchEngine">
    <property name="application" value="Test.App" />
    <property name="server" ref="httpServer" />
    <property name="invoker" ref="invoker" />
</bean>
   
<!--
通知服务, 用于动态调整开关项值,主要协议如下:
1. print, 打印应用的所有开关项(可以指定开关项)
2. modify, 调整开关项内容
3. reload, 从eswitch server同步开关项
-->
<bean id="httpServer" class="com.helijia.framework.eswitch.server.DefaultActionServer">
    <property name="switchEngine" ref="switchEngine" />
</bean>
```

```java
if (switchEngine.isOn("dubbo.inquiry.down", true)) {
     // 询价服务正常, 通过询价服务,计算商品价格
     price = inquiryService.inquiryCommodity(commodityInstance, operateCode);
 } else {
    // 询价服务异常(挂了), 通过本地方法, 计算商品价格
    price = inquiryService.localInquiry(commodityInstance, operateCode);
}
```

#### 流控功能(流控降级)
###### Annotation方式
```xml
<!-- 引入上述三个核心组件 -->
  
<!-- 阀值拦截器: 负责对流量的控制保护你-->
<bean id="thresholdInterceptor" class="com.helijia.framework.eswitch.threshold.ThresholdInterceptor" />
  
<!-- 配置需要保护的服务 -->
<bean id="thresholdPointcut" class="org.springframework.aop.support.JdkRegexpMethodPointcut" >
    <property name="patterns">
        <list>
            <value>com.helijia.framework.eswitch.example.SwitchExample..*</value>
        </list>
    </property>
</bean>
  
<!-- AOP配置 -->
<aop:config>
    <aop:advisor advice-ref="thresholdInterceptor" pointcut-ref="thresholdPointcut" />
</aop:config>
```

```java
@Threshold(item = "dubbo.inquiry", defaultValue = 50)
public class InquiryService {
  
    public void inquiryCommodity(){}
  
    @Threshold(item = "dubbo.inquiry.inquiryCommodity2", defaultValue = 100)
    public void inquiryCommodity2(){}
}
```
通过Threshold Annotation配置服务的阀值. Threshold可以配置在类级别, 表明这个类下所有方法调用的阀值; 也可以配置在方法级别, 表明这个方法调用的阀值.
当调用超过阀值后, 会接受到一个ThresholdException. 业务catch这个异常,可以做预案, 或者直接返回错误.

###### XML方式
```xml
<!-- 流控阀值配置 -->
<bean id="xmlConfigurationStrategy" class="com.helijia.framework.eswitch.threshold.configuration.XmlConfigurationStrategy">
    <property name="config">
        <list>
            <value>com.helijia.framework.eswitch.threshold.Api2#func1 api2.func1 1 </value><!-- 方法级别 -->
            <value>com.helijia.framework.eswitch.threshold.Api3       api3 3       </value><!--  类级别  -->
            <value>com.helijia.framework.eswitch.threshold            api2.func2 2 </value><!--  包级别  -->
        </list>
    </property>
</bean>
  
<!-- 流控拦截器 -->
<bean id="thresholdInterceptor" class="com.helijia.framework.eswitch.threshold.ThresholdInterceptorX">
    <property name="strategies">
        <list>
            <ref local="xmlConfigurationStrategy" />
        </list>
    </property>
</bean>
  
<!-- 流控切面 -->
<bean id="thresholdPointcut" class="org.springframework.aop.support.JdkRegexpMethodPointcut" scope="prototype">
    <property name="patterns">
        <list>
            <value>com.helijia.framework.eswitch.threshold.Api.*</value>
            <value>com.helijia.framework.eswitch.threshold.Api2.*</value>
            <value>com.helijia.framework.eswitch.threshold.Api3.*</value>
        </list>
    </property>
</bean>
  
<aop:config>
    <aop:advisor advice-ref="thresholdInterceptor" pointcut-ref="thresholdPointcut" />
</aop:config>
```

#### 动态调整开关值
下载eswitch代码中的eswitch.py脚本, 使用方式:
```txt
./eswitch.py -h
Usage: eswitch.py [-options] <on/off> <threshold>
 
Options:
  -h, --help            show this help message and exit
  -l HOST, --host=HOST  specify eswitch host
  -p PORT, --port=PORT  specify eswitch port
  -i ITEM, --item=ITEM  specify eswitch item' name
  -c ACTION, --action=ACTION
                        specify eswitch action
```

常用命令实例:
```
* print

    bin/eswitch.py -l localhost -c print
* reload
    bin/eswitch.py -l localhost -a reload

* modify
    bin/eswitch.py -l localhost -c modify -i inquiry.service true 10

    默认端口是30000,如果端口是其他值的话,通过-p参数指定
* bin/eswitch.py -l localhost -p 30001 -c modify -i inquiry.service true 10

    命令执行后,会实时通知ESwitchEngine.
```

#### eswitch console
详见开关管理控制台项目， eswitch-console

#### eswitch性能
* ThresholdInterceptor性能测试情况
```txt
空跑测试

执行次数        耗时(单位毫秒)
100000000       39972
=> 每次拦截增加0.39972微秒的开销
```

```txt
模拟对比测试

API模拟开销-单位毫秒    并发数  每次并发执行API调用数   开销(不加ThresholdInterceptor)-单位毫秒 开销(使用ThresholdInterceptor)-单位毫秒 性能下降(百分比)
1                    20      10000               12048                                 12050                                0.0167%
5                    20      10000               56015                                 56155                                0.2499%
1                    200     10000               12125                                 12161                                0.2969%
5                    200     10000               56576                                 56665                                0.1573%

正常业务场景下,thresholdInterceptor对性能的影响几乎为零;
并发数的上升,对thresholdInterceptor没有影响;
```
