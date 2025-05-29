**Read this in other languages: [中文](README.zh-CN.md).**

# A2A Protocol Agent Server Demo: Sentiment Analysis Agent

## Implementing a Custom Single-Responsibility Agent
If you want to implement your own single-responsibility agent, you can use the following Maven dependency:
```xml
<dependency>
    <groupId>io.github.musaemotion</groupId>
    <artifactId>a2a-agent-server</artifactId>
    <version>0.1.0</version>
</dependency>
```

### Implement the `AgentService` Interface
You need to implement the `AgentService` interface, which defines the core functionality of the agent. Below are detailed explanations of each method in the interface:
- **`supportedContentTypes()`**: This method returns the content types supported by the agent. In this sentiment analysis agent, it mainly supports text types.
- **`agentName()`**: Returns the name of the agent, which should be consistent with the definition in the configuration file and is used to distinguish different agents when pushing messages.
- **`Flux<AgentGeneralResponse> stream(AgentRequest agentRequest)`**: Handles streaming mode requests and returns responses in a streaming manner. This method returns a `Flux` type of `AgentGeneralResponse`, allowing continuous data transmission.
- **`AgentGeneralResponse call(AgentRequest agentRequest)`**: Handles synchronous requests and returns a single response synchronously.

Note that `AgentRequest` and `AgentGeneralResponse` are request and response objects processed by the `a2a-agent-server` proxy.

### Implement `AbstractTaskManager`
You also need to implement `AbstractTaskManager` to manage tasks. However, `a2a-agent-server` already provides an `InMemoryTaskManager` implementation, which you can use directly to simplify the development process.

## Configuration File
The core configuration file of this project uses YAML format. Below is the detailed content and explanation of the configuration file:
```yaml
######################################
# musaemotion A2A Configuration
######################################
musaemotion:
  a2a:
    server:
      # Agent capabilities
      capabilities:
        # Whether push notifications are supported
        push-notifications: true
        # Whether streaming responses are supported
        streaming: true
      # List of skills the agent possesses
      skills:
        - id: 'test-skill'
          name: 'testSkill'
          # Description of the skill
          description: 'Mainly used for sentiment analysis'
          # Usage examples of the skill
          examples:
            - 'Please help me analyze: if I say "I don’t like the taste," does it mean I like it or not?'
      # Access address of the agent service
      url: 'http://127.0.0.1:9999/'
      # Agent version number
      version: '1.0.0'
      # The following are optional configurations, default input and output modes
      #default-input-modes:
      #  - 'text'
      #default-output-modes:
      #  - 'text'
      # Note: The agent name should use English characters
      name: 'SentimentAnalysis'
      # Overall description of the agent
      description: 'Sentiment analysis: positive or negative'
```

With the above configuration, you can flexibly adjust the functions and features of the agent according to actual needs, ensuring that the agent can meet the requirements of different scenarios.