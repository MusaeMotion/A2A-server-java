**其他语言版本: [English](README.md)**

# A2A协议 Agent Server Demo
## 该demo实现了一个视觉识别智能体

## 如果你想实现你自己职责单一的其他智能体 maven dependency
```maven
      <dependency>
            <groupId>io.github.musaemotion</groupId>
            <artifactId>a2a-agent-server</artifactId>
             <version>0.3.1</version>
      </dependency>
```


* 需要实现 AgentService ，下面是方法说明。
     - supportedContentTypes  智能体支持的类型
     - agentName  agent名称
     - Flux<AgentGeneralResponse> stream(AgentRequest agentRequest) 流模式请求
     - AgentGeneralResponse call(AgentRequest agentRequest) 同步请求方法
    > AgentRequest，AgentGeneralResponse 是被 a2a-agent-server 代理之后的请求和响应
* 需要实现 AbstractTaskManager ，当然  a2a-agent-server 实现了一个 InMemoryTaskManager 管理器，你可以直接使用。

## * 配置文件
```yml
######################################
#  musaemotion A2A Card 配置
######################################
musaemotion:
  a2a:
    server:
      capabilities:
        push-notifications: false
        streaming: false
      skills:
        - id: 'visionSkill'
          name: 'visionSkill'
          description: '图像视觉识别'
          examples:
            - '请帮我分析一下这张图片的内容'
      url: 'http://127.0.0.1:9997/'
      version: '1.0.0'
      default-input-modes:
         - 'text'
         - 'image/png'
      default-output-modes:
         - 'text'
      # name 只能使用英文
      name: 'imageVision'
      description: '图像视觉识别'

```
