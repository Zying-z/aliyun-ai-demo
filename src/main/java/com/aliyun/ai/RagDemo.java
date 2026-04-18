package com.aliyun.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class RagDemo {

    private static final String API_KEY = System.getenv("API_KEY");
    private static final String EMBEDDING_URL = "https://dashscope.aliyuncs.com/api/v1/services/embeddings/text-embedding/text-embedding/";
    private static final String CHAT_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";
    private static final String MODEL = "deepseek-v3";

    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .build();

    private static final ObjectMapper mapper = new ObjectMapper();

    // 文档类
    static class Document {
        String text;
        float[] embedding;
        Document(String text, float[] embedding) {
            this.text = text;
            this.embedding = embedding;
        }
    }

    public static void main(String[] args) throws IOException {
        if (API_KEY == null || API_KEY.isEmpty()) {
            System.err.println("错误：请设置环境变量 API_KEY");
            return;
        }

        // 1. 准备知识库
        List<String> knowledgeBase = List.of(
            "RAG（检索增强生成）是一种结合检索和生成的人工智能技术。",
            "RAG可以有效减少大模型的幻觉问题，提高回答的准确性。",
            "RAG的工作流程：先检索相关文档，再基于检索结果生成回答。",
            "向量数据库用于存储文档的向量表示，支持快速相似度检索。",
            "余弦相似度是衡量两个向量相似程度的数学指标，范围在-1到1之间。",
            "Embedding模型可以将文本转换为高维向量，捕获语义信息。",
            "大模型幻觉是指模型生成看似合理但实际错误的内容。",
            "检索增强生成通过引入外部知识来约束模型输出，减少幻觉。"
        );

        // 2. 构建向量库
        System.out.println("=== 第1步：构建知识库向量 ===");
        List<Document> vectorStore = new ArrayList<>();
        for (String doc : knowledgeBase) {
            System.out.println("向量化: " + doc.substring(0, Math.min(40, doc.length())) + "...");
            float[] embedding = getEmbedding(doc);
            vectorStore.add(new Document(doc, embedding));
        }
        System.out.println("知识库构建完成，共 " + vectorStore.size() + " 条文档\n");

        // 3. 用户提问
        Scanner scanner = new Scanner(System.in);
        System.out.print("请输入问题: ");
        String question = scanner.nextLine();

        // 4. 检索相关文档
        System.out.println("\n=== 第2步：检索相关文档 ===");
        float[] questionEmbedding = getEmbedding(question);
        List<String> retrievedDocs = retrieve(vectorStore, questionEmbedding, 3);
        
        System.out.println("检索到的相关文档:");
        for (int i = 0; i < retrievedDocs.size(); i++) {
            System.out.println((i + 1) + ". " + retrievedDocs.get(i));
        }

        // 5. 生成回答
        System.out.println("\n=== 第3步：生成回答 ===");
        String answer = generateAnswer(question, retrievedDocs);
        System.out.println("\n=== 最终回答 ===");
        System.out.println(answer);
    }

    /**
     * 检索：返回最相似的 Top-K 文档内容
     */
    public static List<String> retrieve(List<Document> vectorStore, float[] queryEmbedding, int topK) {
        List<SearchResult> results = new ArrayList<>();
        for (Document doc : vectorStore) {
            double similarity = cosineSimilarity(queryEmbedding, doc.embedding);
            results.add(new SearchResult(doc.text, similarity));
        }
        
        // 按相似度降序排序
        results.sort((a, b) -> Double.compare(b.similarity, a.similarity));
        
        // 返回 Top-K 的文档内容
        List<String> topDocs = new ArrayList<>();
        for (int i = 0; i < Math.min(topK, results.size()); i++) {
            topDocs.add(results.get(i).text);
            System.out.printf("  相似度 %.4f: %s%n", results.get(i).similarity, results.get(i).text);
        }
        return topDocs;
    }

    /**
     * 生成：基于检索结果调用大模型
     */
    public static String generateAnswer(String question, List<String> contexts) throws IOException {
        // 拼接上下文
        StringBuilder contextStr = new StringBuilder();
        for (int i = 0; i < contexts.size(); i++) {
            contextStr.append((i + 1)).append(". ").append(contexts.get(i)).append("\n");
        }

        // 构建 Prompt（提示词）
        String prompt = String.format("""
            你是一个基于知识库的问答助手。请根据以下已知信息回答用户的问题。
            如果根据已知信息无法回答，请说"根据现有知识库无法回答该问题"，不要编造答案。

            === 已知信息 ===
            %s
            === 用户问题 ===
            %s
            === 回答 ===
            """, contextStr.toString(), question);

        // 构建请求体
        String json = String.format("""
                {
                    "model": "%s",
                    "messages": [
                        {"role": "system", "content": "你是一个专业的AI助手，回答要准确、简洁。"},
                        {"role": "user", "content": "%s"}
                    ],
                    "stream": false
                }
                """, MODEL, escapeJson(prompt));

        Request request = new Request.Builder()
                .url(CHAT_URL)
                .header("Authorization", "Bearer " + API_KEY)
                .header("Content-Type", "application/json")
                .post(RequestBody.create(json, MediaType.parse("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                return "生成回答失败: HTTP " + response.code();
            }

            String responseBody = response.body().string();
            JsonNode root = mapper.readTree(responseBody);
            
            // 兼容两种响应格式
            String answer = null;
            if (root.has("output")) {
                answer = root.path("output").path("choices").path(0).path("message").path("content").asText();
            } else if (root.has("choices")) {
                answer = root.path("choices").path(0).path("message").path("content").asText();
            }
            
            return answer != null && !answer.isEmpty() ? answer : "无法解析回答";
        }
    }

    /**
     * 转义 JSON 字符串中的特殊字符
     */
    private static String escapeJson(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /**
     * 调用 Embedding API 获取向量
     */
    public static float[] getEmbedding(String text) throws IOException {
        String json = String.format("""
                {
                    "model": "text-embedding-v3",
                    "input": {
                        "texts": ["%s"]
                    }
                }
                """, escapeJson(text));

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
     * 计算余弦相似度
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

    // 搜索结果辅助类
    static class SearchResult {
        String text;
        double similarity;
        SearchResult(String text, double similarity) {
            this.text = text;
            this.similarity = similarity;
        }
    }
}