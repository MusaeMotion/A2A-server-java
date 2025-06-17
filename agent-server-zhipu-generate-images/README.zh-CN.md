**其他语言版本: [English](README.md)**

# A2A 协议 Agent Server Demo

## 本 Demo 实现了一个文生图智能体

## 如何实现自定义的单一职责智能体

在你的项目中添加如下 Maven 依赖：

```xml
<dependency>
    <groupId>io.github.musaemotion</groupId>
    <artifactId>a2a-agent-server</artifactId>
    <version>0.2.9</version>
</dependency>
```

- 你需要实现 `AgentService` 接口，主要方法说明如下：
    - `supportedContentTypes`：智能体支持的内容类型
    - `agentName`：智能体名称
    - `Flux<AgentGeneralResponse> stream(AgentRequest agentRequest)`：流式请求处理
    - `AgentGeneralResponse call(AgentRequest agentRequest)`：同步请求处理
    > `AgentRequest` 和 `AgentGeneralResponse` 是由 a2a-agent-server 代理的请求和响应对象
- 你还需要实现 `AbstractTaskManager`。a2a-agent-server 已内置 `InMemoryTaskManager`，可直接使用，简化开发。

## 配置文件示例

```yaml
######################################
# musaemotion A2A 配置
######################################
musaemotion:
  a2a:
    server:
      capabilities:
        push-notifications: false
        streaming: false
      skills:
        - id: 'image-skill'
          name: 'imageGenerations'
          description: '用于图片创作生成'
          examples:
            - '请帮我生成一副关于爱情的图片，要求是可爱风格。'
      url: 'http://127.0.0.1:9998/'
      version: '1.0.0'
      default-input-modes:
        - 'text'
      default-output-modes:
        - 'image/png'
      # name 只能使用英文
      name: 'imageGenerations'
      description: '可以使用文字内容生成图片，并且可以定义图片的风格。'
```