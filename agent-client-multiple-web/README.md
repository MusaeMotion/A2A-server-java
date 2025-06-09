**Other languages: [中文](README.zh-CN.md)**

# A2A Protocol Multi-Remote Agent DEMO

## 1. Maven Dependency

If you want to implement your own host-agent, simply add the following Maven dependency to your project:

```xml
<dependency>
    <groupId>io.github.musaemotion</groupId>
    <artifactId>a2a-host-agent</artifactId>
    <version>0.2.0</version>
</dependency>
```

## 2. Implement Four Managers

- `AbstractConversationManager` (Conversation Management)
- `AbstractMessageManager` (Message Management)
- `AbstractRemoteAgentManager` (Remote Agent Management)
- `AbstractTaskCenterManager` (Task Management for Remote Agent Calls)

## 3. DEMO Supports MySQL

- The DEMO relies on Flyway for automatic database initialization. The SQL scripts are exported from MySQL 8.0.

## 4. Example Configuration File

```yaml
######################################
# musaemotion A2A Configuration
######################################
musaemotion:
  a2a:
    host-agent:
      # If this address is set, a notify service will be started by default; if not set, it will not start. Used for remote agent callback notifications.
      notify-url: http://localhost:5000
      # Remote agent addresses automatically loaded at startup
      remote-agent-addresses:
        - http://127.0.0.1:9999/
```

### Notes

- This DEMO enables Spring Observations by default. Please use Docker to run an openzipkin container and ensure http://127.0.0.1:9411 is accessible.
- If you do not need Spring Observations, you can remove the following Maven dependencies:

```xml
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