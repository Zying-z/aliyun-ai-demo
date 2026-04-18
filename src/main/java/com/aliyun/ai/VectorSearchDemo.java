package com.aliyun.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class VectorSearchDemo {

    private static final String API_KEY = System.getenv("API_KEY");
    private static final String EMBEDDING_URL = "https://dashscope.aliyuncs.com/api/v1/services/embeddings/text-embedding/text-embedding/";

    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .build();

    private static final ObjectMapper mapper = new ObjectMapper();

    // 文档类：存储原始文本和对应的向量
    static class Document {
        String text;
        float[] embedding;

        Document(String text, float[] embedding) {
            this.text = text;
            this.embedding = embedding;
        }
    }

    // 搜索结果类：存储文档文本和相似度
    static class SearchResult {
        String text;
        double similarity;

        SearchResult(String text, double similarity) {
            this.text = text;
            this.similarity = similarity;
        }
    }

    public static void main(String[] args) throws IOException {
        if (API_KEY == null || API_KEY.isEmpty()) {
            System.err.println("错误：请设置环境变量 API_KEY");
            return;
        }

        // 1. 准备文档库（模拟知识库）
        List<String> documents = List.of(
            "RAG（检索增强生成）是一种结合检索和生成的人工智能技术。",
            "Java是一种面向对象的编程语言，由Sun公司于1995年推出。",
            "向量数据库用于存储文档的向量表示，支持快速相似度检索。",
            "Spring Boot是Java生态中最流行的微服务框架之一。",
            "余弦相似度是衡量两个向量方向相似程度的数学指标。",
            "Python是一种解释型、面向对象的高级编程语言。",
            "RAG可以有效减少大模型的幻觉问题，提高回答的准确性。",
            "MySQL是最流行的开源关系型数据库管理系统。"
        );

        System.out.println("=== 构建向量库 ===");
        List<Document> vectorStore = new ArrayList<>();
        for (String doc : documents) {
            System.out.println("正在向量化: " + doc.substring(0, Math.min(30, doc.length())) + "...");
            float[] embedding = getEmbedding(doc);
            vectorStore.add(new Document(doc, embedding));
        }
        System.out.println("向量库构建完成，共 " + vectorStore.size() + " 条文档\n");

        // 2. 用户查询
        Scanner scanner = new Scanner(System.in);
        System.out.print("请输入搜索内容: ");
        String query = scanner.nextLine();

        // 3. 将查询向量化
        System.out.println("正在处理查询...");
        float[] queryEmbedding = getEmbedding(query);

        // 4. 暴力搜索：计算每个文档的相似度
        List<SearchResult> results = new ArrayList<>();
        for (Document doc : vectorStore) {
            double similarity = cosineSimilarity(queryEmbedding, doc.embedding);
            results.add(new SearchResult(doc.text, similarity));
        }

        // 5. 按相似度降序排序
        results.sort((a, b) -> Double.compare(b.similarity, a.similarity));

        // 6. 输出 Top-3 结果
        int topK = 3;
        System.out.println("\n=== 搜索结果 (Top " + topK + ") ===");
        System.out.println("查询: " + query);
        System.out.println();
        for (int i = 0; i < Math.min(topK, results.size()); i++) {
            SearchResult r = results.get(i);
            System.out.printf("%d. [相似度: %.4f] %s%n", i + 1, r.similarity, r.text);
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
                throw new IOException("HTTP " + response.code());
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