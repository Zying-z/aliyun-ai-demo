package com.aliyun.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;

import java.io.IOException;

public class EmbeddingSimilarityDemo {

    private static final String API_KEY = System.getenv("API_KEY");
    private static final String EMBEDDING_URL = "https://dashscope.aliyuncs.com/api/v1/services/embeddings/text-embedding/text-embedding/";

    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .build();

    private static final ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) throws IOException {
        if (API_KEY == null || API_KEY.isEmpty()) {
            System.err.println("错误：请设置环境变量 API_KEY");
            return;
        }

        // 1. 准备两个文本
        String text1 = "我喜欢吃苹果";
        String text2 = "我爱吃苹果";

        System.out.println("=== 计算两个文本的相似度 ===\n");
        System.out.println("文本1: " + text1);
        System.out.println("文本2: " + text2);
        System.out.println();

        // 2. 获取两个文本的向量
        System.out.println("正在获取文本1的向量...");
        float[] embedding1 = getEmbedding(text1);
        System.out.println("向量维度: " + embedding1.length);

        System.out.println("正在获取文本2的向量...");
        float[] embedding2 = getEmbedding(text2);
        System.out.println("向量维度: " + embedding2.length);
        System.out.println();

        // 3. 计算余弦相似度
        double similarity = cosineSimilarity(embedding1, embedding2);

        System.out.println("=== 结果 ===");
        System.out.println("余弦相似度: " + similarity);

        // 4. 解释相似度含义
        System.out.println("\n=== 解释 ===");
        if (similarity > 0.8) {
            System.out.println("相似度很高（>0.8），两个文本语义非常接近");
        } else if (similarity > 0.6) {
            System.out.println("相似度较高（>0.6），两个文本语义相关");
        } else if (similarity > 0.4) {
            System.out.println("相似度中等（>0.4），两个文本有一定关联");
        } else {
            System.out.println("相似度较低（<0.4），两个文本语义差异较大");
        }
    }

    /**
     * 调用 Embedding API，将文本转换为向量
     */
    public static float[] getEmbedding(String text) throws IOException {
        String json = String.format("""
                {
                    "model": "text-embedding-v3",
                    "input": {
                        "texts": ["%s"]
                    }
                }
                """, text);

        Request request = new Request.Builder()
                .url(EMBEDDING_URL)
                .header("Authorization", "Bearer " + API_KEY)
                .header("Content-Type", "application/json")
                .post(RequestBody.create(json, MediaType.parse("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("HTTP " + response.code() + ": " +
                        (response.body() != null ? response.body().string() : "无响应体"));
            }

            String body = response.body().string();
            JsonNode root = mapper.readTree(body);
            JsonNode embeddingNode = root.path("output").path("embeddings").path(0).path("embedding");

            float[] result = new float[embeddingNode.size()];
            for (int i = 0; i < embeddingNode.size(); i++) {
                result[i] = Float.parseFloat(embeddingNode.get(i).asText());
            }
            return result;
        }
    }

    /**
     * 计算两个向量的余弦相似度
     */
    public static double cosineSimilarity(float[] a, float[] b) {
        if (a.length != b.length) {
            throw new IllegalArgumentException("向量维度必须相同");
        }

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < a.length; i++) {
            dotProduct += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }

        if (normA == 0 || normB == 0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}
