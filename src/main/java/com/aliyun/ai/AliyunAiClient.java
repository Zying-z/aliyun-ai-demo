package com.aliyun.ai;

import okhttp3.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Scanner;

public class AliyunAiClient {

    // 从环境变量读取 API Key（需要在运行配置中设置 API_KEY）
    private static final String API_KEY = System.getenv("API_KEY");

    public static void main(String[] args) throws IOException {
        if (API_KEY == null || API_KEY.isEmpty()) {
            System.err.println("错误: 请设置环境变量 API_KEY");
            return;
        }

        Scanner scanner = new Scanner(System.in);
        System.out.print("请输入你的问题: ");
        String userMessage = scanner.nextLine();

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .build();

        ObjectMapper mapper = new ObjectMapper();

        String json = String.format("""
                {
                    "model": "deepseek-v3",
                    "messages": [
                        {"role": "user", "content": "%s"}
                    ],
                    "stream": false
                }
                """, userMessage);

        Request request = new Request.Builder()
                .url("https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions")
                .header("Authorization", "Bearer " + API_KEY)
                .header("Content-Type", "application/json")
                .post(RequestBody.create(json, MediaType.parse("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                System.out.println("HTTP状态码: " + response.code());
                System.out.println("错误信息: " + response.body().string());
                return;
            }

            String responseBody = response.body().string();
            JsonNode root = mapper.readTree(responseBody);

            String aiReply = null;
            if (root.has("output")) {
                aiReply = root.path("output").path("choices").path(0).path("message").path("content").asText();
            } else if (root.has("choices")) {
                aiReply = root.path("choices").path(0).path("message").path("content").asText();
            }

            if (aiReply != null && !aiReply.isEmpty()) {
                System.out.println("AI回答: " + aiReply);
            } else {
                System.out.println("无法解析AI回答，原始响应: " + responseBody);
            }
        }
    }
}