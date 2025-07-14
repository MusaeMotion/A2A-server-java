**其他语言版本: [English](README.md)**

# A2A协议 multiple 远程智能体DEMO
## 1. maven dependency

如果你需要实现自己的host-agent, 只需要maven dependency： a2a-host-agent

```maven
    <dependency>
        <groupId>io.github.musaemotion</groupId>
        <artifactId>a2a-host-agent</artifactId>
         <version>0.3.3</version>
    </dependency>
```
 
## * 实现四个管理器
* AbstractConversationManager （对话管理）
* AbstractMessageManager (对话消息管理)
* AbstractRemoteAgentManager (远程智能体管理)
* AbstractTaskCenterManager (远程智能体调用产生的相关任务管理)

## * 本DEMO实现了Mysql
### * DEMO dependency： flyway自动初始化数据库，sql脚本由Mysql 8.0 导出。
## * 配置文件
```yml
######################################
# musaemotion A2A配置
######################################
musaemotion:
 a2a:
  host-agent:
   # 如果设置该地址会默认开启一个notify服务，如果不设置该项，默认不启动，用于远程智能体回调通知使用
   notify-url: http://localhost:5000
   # 默认启动时自动载入的远程智能体地址
   remote-agent-addresses:
    - http://127.0.0.1:9999/
```

### * 注意：该DEMO 默认开启了 Spring Observations，所以请使用docker 运行一个 openzipkin 容器保证 http://127.0.0.1:9411 能访问
### * 如果不想使用 Spring Observations 则删除掉以下maven引用
```maven
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-tracing-bridge-brave</artifactId>
</dependency>
<dependency>
    <groupId>io.zipkin.reporter2</groupId>
    <artifactId>zipkin-reporter-brave</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```
