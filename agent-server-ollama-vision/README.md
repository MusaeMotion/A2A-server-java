**Other languages: [中文](README.zh-CN.md)**

# A2A Protocol Agent Server Demo

## This demo implements a vision recognition agent

## If you want to implement your own single-responsibility agent, add the following Maven dependency:
```xml
<dependency>
    <groupId>io.github.musaemotion</groupId>
    <artifactId>a2a-agent-server</artifactId>
    <version>0.2.2</version>
</dependency>
```

- You need to implement the `AgentService` interface. The main methods are:
    - `supportedContentTypes`: Content types supported by the agent
    - `agentName`: The name of the agent
    - `Flux<AgentGeneralResponse> stream(AgentRequest agentRequest)`: Stream mode request
    - `AgentGeneralResponse call(AgentRequest agentRequest)`: Synchronous request method
    > `AgentRequest` and `AgentGeneralResponse` are the request and response objects proxied by a2a-agent-server.
- You also need to implement `AbstractTaskManager`. Of course, a2a-agent-server provides an `InMemoryTaskManager` implementation that you can use directly.

## Configuration file example
```yaml
######################################
# musaemotion A2A Card Configuration
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
          description: 'Image vision recognition'
          examples:
            - 'Please help me analyze the content of this image'
      url: 'http://127.0.0.1:9997/'
      version: '1.0.0'
      default-input-modes:
        - 'text'
        - 'image/png'
      default-output-modes:
        - 'text'
      # The name should use English only
      name: 'imageVision'
      description: 'Image vision recognition'
```