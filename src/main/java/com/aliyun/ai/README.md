# 阿里云百炼 API Java 调用示例

## 项目简介
本项目演示如何用 Java + OkHttp 调用阿里云百炼大模型 API，包含同步调用和流式输出两种方式。

## 技术栈
- Java 17
- OkHttp 4.12.0
- Jackson 2.16.1

## 环境配置
1. 在阿里云百炼控制台获取 API Key
2. 在 IDEA 运行配置中设置环境变量：`API_KEY=sk-xxx`

## 运行方式
- `AliyunAiClient`：同步调用，一次性返回完整回答
- `AliyunAiClientStream`：流式调用，逐字打印（打字机效果）

## 代码结构
src/main/java/com/aliyun/ai/
├── AliyunAiClient.java // 同步调用
└── AliyunAiClientStream.java // 流式调用

## 效果演示

同步调用
D:\APP\java1\jdk-17.0.12\bin\java.exe "-javaagent:D:\APP\java1\IntelliJ IDEA 2025.2\lib\idea_rt.jar=63855" -Dfile.encoding=UTF-8 -classpath C:\Users\26435\Desktop\aliyun-ai-demo\target\classes;D:\APP\java1\mvnrep\com\squareup\okhttp3\okhttp\4.12.0\okhttp-4.12.0.jar;D:\APP\java1\mvnrep\com\squareup\okio\okio\3.6.0\okio-3.6.0.jar;D:\APP\java1\mvnrep\com\squareup\okio\okio-jvm\3.6.0\okio-jvm-3.6.0.jar;D:\APP\java1\mvnrep\org\jetbrains\kotlin\kotlin-stdlib-common\1.9.10\kotlin-stdlib-common-1.9.10.jar;D:\APP\java1\mvnrep\org\jetbrains\kotlin\kotlin-stdlib-jdk8\1.8.21\kotlin-stdlib-jdk8-1.8.21.jar;D:\APP\java1\mvnrep\org\jetbrains\kotlin\kotlin-stdlib\1.8.21\kotlin-stdlib-1.8.21.jar;D:\APP\java1\mvnrep\org\jetbrains\annotations\13.0\annotations-13.0.jar;D:\APP\java1\mvnrep\org\jetbrains\kotlin\kotlin-stdlib-jdk7\1.8.21\kotlin-stdlib-jdk7-1.8.21.jar;D:\APP\java1\mvnrep\com\fasterxml\jackson\core\jackson-databind\2.16.1\jackson-databind-2.16.1.jar;D:\APP\java1\mvnrep\com\fasterxml\jackson\core\jackson-annotations\2.16.1\jackson-annotations-2.16.1.jar;D:\APP\java1\mvnrep\com\fasterxml\jackson\core\jackson-core\2.16.1\jackson-core-2.16.1.jar com.aliyun.ai.AliyunAiClient
请输入你的问题: 你是谁
AI回答: 我是DeepSeek Chat，由深度求索公司创造的AI助手！✨ 我可以帮你解答问题、整理资料、写作、编程，甚至陪你聊天~ 有什么我可以帮你的吗？😊

进程已结束，退出代码为 0

流式调用
D:\APP\java1\jdk-17.0.12\bin\java.exe "-javaagent:D:\APP\java1\IntelliJ IDEA 2025.2\lib\idea_rt.jar=49674" -Dfile.encoding=UTF-8 -classpath C:\Users\26435\Desktop\aliyun-ai-demo\target\classes;D:\APP\java1\mvnrep\com\squareup\okhttp3\okhttp\4.12.0\okhttp-4.12.0.jar;D:\APP\java1\mvnrep\com\squareup\okio\okio\3.6.0\okio-3.6.0.jar;D:\APP\java1\mvnrep\com\squareup\okio\okio-jvm\3.6.0\okio-jvm-3.6.0.jar;D:\APP\java1\mvnrep\org\jetbrains\kotlin\kotlin-stdlib-common\1.9.10\kotlin-stdlib-common-1.9.10.jar;D:\APP\java1\mvnrep\org\jetbrains\kotlin\kotlin-stdlib-jdk8\1.8.21\kotlin-stdlib-jdk8-1.8.21.jar;D:\APP\java1\mvnrep\org\jetbrains\kotlin\kotlin-stdlib\1.8.21\kotlin-stdlib-1.8.21.jar;D:\APP\java1\mvnrep\org\jetbrains\annotations\13.0\annotations-13.0.jar;D:\APP\java1\mvnrep\org\jetbrains\kotlin\kotlin-stdlib-jdk7\1.8.21\kotlin-stdlib-jdk7-1.8.21.jar;D:\APP\java1\mvnrep\com\fasterxml\jackson\core\jackson-databind\2.16.1\jackson-databind-2.16.1.jar;D:\APP\java1\mvnrep\com\fasterxml\jackson\core\jackson-annotations\2.16.1\jackson-annotations-2.16.1.jar;D:\APP\java1\mvnrep\com\fasterxml\jackson\core\jackson-core\2.16.1\jackson-core-2.16.1.jar com.aliyun.ai.AliyunAiClientStream
请输入你的问题：你是谁
我是DeepSeek Chat，由深度求索公司（DeepSeek）研发的AI助手！🤖✨ 我可以帮你解答问题、提供知识、陪你聊天，甚至帮你处理各种文本和文件内容。无论是学习、工作还是日常生活中的疑问，都可以来问我哦！😊

有什么我可以帮你的吗？

进程已结束，退出代码为 0



