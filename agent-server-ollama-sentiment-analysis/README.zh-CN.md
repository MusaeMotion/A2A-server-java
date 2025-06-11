**其他语言版本: [English](README.md)**

# A2A 协议代理服务器演示：情感分析智能体

## 实现自定义单一职责智能体
如果你希望实现自己的单一职责智能体，可以使用以下 Maven 依赖：
```xml
<dependency>
    <groupId>io.github.musaemotion</groupId>
    <artifactId>a2a-agent-server</artifactId>
    <version>0.2.5</version>
</dependency>
```

### 实现 `AgentService` 接口
你需要实现 `AgentService` 接口，该接口定义了智能体的核心功能。以下是对接口中各方法的详细说明：
- **`supportedContentTypes()`**：此方法用于返回智能体所支持的内容类型。在本情感分析智能体中，主要支持文本类型。
- **`agentName()`**：返回智能体的名称，该名称应与配置文件中的定义保持一致，用于消息推送时区分不同的智能体。
- **`Flux<AgentGeneralResponse> stream(AgentRequest agentRequest)`**：处理流模式请求，以流的方式返回响应结果。该方法返回一个 `Flux` 类型的 `AgentGeneralResponse`，允许持续不断地传输数据。
- **`AgentGeneralResponse call(AgentRequest agentRequest)`**：处理同步请求，以同步方式返回单个响应结果。

需要注意的是，`AgentRequest` 和 `AgentGeneralResponse` 是经过 `a2a-agent-server` 代理处理后的请求和响应对象。

### 实现 `AbstractTaskManager`
你还需要实现 `AbstractTaskManager` 来管理任务。不过，`a2a-agent-server` 已经提供了一个 `InMemoryTaskManager` 实现，你可以直接使用该管理器来简化开发过程。

## 配置文件
本项目的核心配置文件采用 YAML 格式，以下是配置文件的详细内容及说明：
```yaml
######################################
# musaemotion A2A 配置
######################################
musaemotion:
  a2a:
    server:
      # 智能体的能力特性
      capabilities:
        # 是否支持推送通知
        push-notifications: true
        # 是否支持流式响应
        streaming: true
      # 智能体所具备的技能列表
      skills:
        - id: 'test-skill'
          name: 'testSkill'
          # 技能的描述信息
          description: '主要是用于情感分析'
          # 技能的使用示例
          examples:
            - '请帮我分析一下 我觉的这个不好吃是喜欢吃还是不喜欢吃'
      # 智能体服务的访问地址
      url: 'http://127.0.0.1:9999/'
      # 智能体的版本号
      version: '1.0.0'
      # 以下为可选配置，默认输入和输出模式
      #default-input-modes:
      #  - 'text'
      #default-output-modes:
      #  - 'text'
      # 注意：智能体名称需使用英文字符
      name: 'SentimentAnalysis'
      # 智能体的整体描述
      description: '情感分析是否正向反向'
```

通过上述配置，你可以根据实际需求对智能体的各项功能和特性进行灵活调整，确保智能体能够满足不同场景的使用要求。