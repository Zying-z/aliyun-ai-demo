package com.aliyun.ai;

import okhttp3.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Scanner;
import java.io.IOException;

public class AliyunAiClientStream {

    private static final String API_KEY = System.getenv("API_KEY");
    private static final String API_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";
    private static final String MODEL = "deepseek-v3";

    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .build();

    private static final ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) throws IOException {
        if (API_KEY == null || API_KEY.isEmpty()) {
            System.err.println("错误：未设置环境变量 API_KEY");
            return;
        }

        Scanner scanner = new Scanner(System.in);
        System.out.print("请输入你的问题：");
        String userMessage = scanner.nextLine();

        chatStream(userMessage);
    }

    public static void chatStream(String userMessage) throws IOException {
        String json = String.format("""
                {
                    "model": "%s",
                    "messages": [
                        {"role": "user", "content": "%s"}
                    ],
                    "stream": true
                }
                """, MODEL, userMessage);

        Request request = new Request.Builder()
                .url(API_URL)
                .header("Authorization", "Bearer " + API_KEY)
                .header("Content-Type", "application/json")
                .post(RequestBody.create(json, MediaType.parse("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                System.err.println("HTTP 错误：" + response.code());
                return;
            }

            okhttp3.ResponseBody body = response.body();
            if (body == null) return;

            okio.BufferedSource source = body.source();
            while (!source.exhausted()) {
                String line = source.readUtf8Line();
                if (line == null || !line.startsWith("data:")) continue;

                String data = line.substring(5).trim();
                if (data.isEmpty()) continue;
                if ("[DONE]".equals(data)) {
                    System.out.println();
                    break;
                }

                try {
                    JsonNode node = mapper.readTree(data);
                    String content = node.path("choices").path(0).path("delta").path("content").asText();
                    if (content != null && !content.isEmpty()) {
                        System.out.print(content);
                        System.out.flush();
                    }
                } catch (Exception e) {
                    // 忽略解析错误
                }
            }
        }
    }
}