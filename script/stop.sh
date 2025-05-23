#!/bin/bash

# 目标目录（请修改为你的JAR文件所在目录）
TARGET_DIR=`pwd`

# 进入目标目录
cd "$TARGET_DIR" || exit

# 循环处理目录中的所有JAR文件
for jar_file in *.jar; do
    # 获取不包含扩展名的文件名作为服务名
    service_name="${jar_file%.jar}"

    # 检查是否有相关进程正在运行
    pids=$(ps -ef | grep "[j]ava" | grep "$jar_file" | awk '{print $2}')

    if [ -n "$pids" ]; then
        echo "发现正在运行的进程：$jar_file"

        # 停止所有相关进程
        for pid in $pids; do
            echo "正在停止进程 ID: $pid"
            kill -9 "$pid" || echo "停止进程 $pid 失败"
        done
    else
        echo "没有发现正在运行的进程：$jar_file"
    fi

done

echo "所有JAR文件处理完成"
