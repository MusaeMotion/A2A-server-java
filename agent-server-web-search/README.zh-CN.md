**其他语言版本: [English](README.md)**

# A2A协议 Agent Server Demo

## 本 Demo 实现了一个 Web 搜索智能体

## 如何实现自定义的单一职责智能体

在你的项目中添加如下 Maven 依赖：

```xml
<dependency>
    <groupId>io.github.musaemotion</groupId>
    <artifactId>a2a-agent-server</artifactId>
  <version>0.1.1</version>
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
#  musaemotion A2A Card 配置
######################################
musaemotion:
  a2a:
    server:
      capabilities:
        push-notifications: false
        streaming: false
      skills:
        - id: 'webSearchSkill'
          name: 'webSearchSkill'
          description: '搜索引擎搜索'
          examples:
            - '请给我搜索一下刘德华的新闻'
      url: ${card-url}:${server.port}
      version: '1.0.0'
      default-input-modes:
         - 'text'
         - 'image/png'
      default-output-modes:
         - 'text'
         - 'image/png'
      # name 只能使用英文
      name: 'webSearch'
      description: '在搜索引擎里搜索内容、文章、新闻、图片'
```