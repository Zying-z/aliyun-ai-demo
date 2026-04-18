package com.aliyun.ai;

import okhttp3.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MultiTurnChat {

    private static final String API_KEY = System.getenv("API_KEY");
    private static final String API_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";
    private static final String MODEL = "deepseek-v3";

    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .build();

    private static final ObjectMapper mapper = new ObjectMapper();

    static class Message {
        String role;
        String content;

        Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }

    public static void main(String[] args) throws IOException {
        if (API_KEY == null || API_KEY.isEmpty()) {
            System.err.println("Please set API_KEY");
            return;
        }

        List<Message> history = new ArrayList<>();

        Scanner scanner = new Scanner(System.in);
        System.out.println("多轮对话已开启，输入 'exit' 退出");
        while (true) {
            System.out.print("你： ");
            String userInput = scanner.nextLine();
            if ("exit".equalsIgnoreCase(userInput)) {
                System.out.println("对话结束");
                break;
            }

            history.add(new Message("user", userInput));
            String aiReply = chat(history);
            history.add(new Message("assistant", aiReply));

            System.out.println("AI: " + aiReply);
            System.out.println();
        }
    }

    public static String chat(List<Message> history) throws IOException {
        StringBuilder messagesBuilder = new StringBuilder();
        messagesBuilder.append("[");
        for (int i = 0; i < history.size(); i++) {
            Message msg = history.get(i);
            if (i > 0) {
                messagesBuilder.append(",");
            }
            messagesBuilder.append(String.format(
                    "{\"role\":\"%s\",\"content\":\"%s\"}",
                    msg.role, msg.content
            ));
        }
        messagesBuilder.append("]");

        String json = String.format("""
                {
                    "model": "%s",
                    "messages": %s,
                    "stream": false
                }
                """, MODEL, messagesBuilder.toString());

        Request request = new Request.Builder()
                .url(API_URL)
                .header("Authorization", "Bearer " + API_KEY)
                .header("Content-Type", "application/json")
                .post(RequestBody.create(json, MediaType.parse("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "无响应体";
                throw new IOException("HTTP " + response.code() + ": " + errorBody);
            }

            String responseBody = response.body().string();
            JsonNode root = mapper.readTree(responseBody);


            String aiReply = root.path("choices").path(0).path("message").path("content").asText();

            if (aiReply == null || aiReply.isEmpty()) {
                throw new IOException("无法解析AI回答，原始响应: " + responseBody);
            }
            return aiReply;
        }
    }
}