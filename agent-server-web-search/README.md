**Other languages: [中文](README.zh-CN.md)**

# A2A Protocol Agent Server Demo

## This demo implements a Web Search Agent

## How to Implement a Custom Single-Responsibility Agent

Add the following Maven dependency to your project:

```xml
<dependency>
    <groupId>io.github.musaemotion</groupId>
    <artifactId>a2a-agent-server</artifactId>
     <version>0.2.11</version>
</dependency>
```

- You need to implement the `AgentService` interface. The main methods are as follows:
    - `supportedContentTypes`: Content types supported by the agent
    - `agentName`: The name of the agent
    - `Flux<AgentGeneralResponse> stream(AgentRequest agentRequest)`: Stream request handling
    - `AgentGeneralResponse call(AgentRequest agentRequest)`: Synchronous request handling
    > `AgentRequest` and `AgentGeneralResponse` are request and response objects proxied by a2a-agent-server.
- You also need to implement `AbstractTaskManager`. The a2a-agent-server provides a built-in `InMemoryTaskManager` that you can use directly to simplify development.

## Example Configuration File

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
      name: 'webSearch'
      description: '在搜索引擎里搜索内容、文章、新闻、图片'
```