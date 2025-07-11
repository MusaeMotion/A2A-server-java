FROM openjdk:17-jdk-alpine
# 将工作目录设置为 /app
WORKDIR /app

# 将编译好的 jar 文件复制到 /app 目录下
COPY target/host-agent-web-server.jar app.jar

# 暴露 10001 5000 端口
EXPOSE 10001 5000

# 运行 jar 文件
ENTRYPOINT ["java", "-jar", "-Xmx512m", "app.jar"]