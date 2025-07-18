**Other languages: [中文](README.zh-CN.md)**

# A2A Protocol Agent Server Demo

## This demo implements a text-to-image generation agent

## How to implement your own single-responsibility agent

Add the following Maven dependency to your project:

```xml
<dependency>
    <groupId>io.github.musaemotion</groupId>
    <artifactId>a2a-agent-server</artifactId>
      <version>0.3.5</version>
</dependency>
```

- You need to implement the `AgentService` interface. The main methods are as follows:
    - `supportedContentTypes`: Content types supported by the agent
    - `agentName`: The name of the agent
    - `Flux<AgentGeneralResponse> stream(AgentRequest agentRequest)`: Stream request handling
    - `AgentGeneralResponse call(AgentRequest agentRequest)`: Synchronous request handling
    > `AgentRequest` and `AgentGeneralResponse` are request and response objects proxied by a2a-agent-server.
- You also need to implement `AbstractTaskManager`. The a2a-agent-server provides a built-in `InMemoryTaskManager` that you can use directly to simplify development.

## Example configuration file

```yaml
######################################
# musaemotion A2A Configuration
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
          description: 'Used for image creation and generation'
          examples:
            - 'Please generate a cute-style image about love.'
      url: 'http://127.0.0.1:9998/'
      version: '1.0.0'
      default-input-modes:
        - 'text'
      default-output-modes:
        - 'image/png'
      # The name should use English only
      name: 'imageGenerations'
      description: 'Generate images from text content, and define the style of the image.'
```