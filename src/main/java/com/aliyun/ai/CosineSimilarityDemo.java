package com.aliyun.ai;

public class CosineSimilarityDemo {

    public static void main(String[] args) {
        // 示例向量
        float[] vectorA = {1.0f, 2.0f, 3.0f};
        float[] vectorB = {2.0f, 4.0f, 6.0f};  // 与 A 方向相同
        float[] vectorC = {1.0f, 0.0f, 0.0f};  // 与 A 方向不同
        float[] vectorD = {-1.0f, -2.0f, -3.0f}; // 与 A 方向相反

        System.out.println("向量 A: [1.0, 2.0, 3.0]");
        System.out.println("向量 B: [2.0, 4.0, 6.0]（与 A 同方向）");
        System.out.println("向量 C: [1.0, 0.0, 0.0]（与 A 不同方向）");
        System.out.println("向量 D: [-1.0, -2.0, -3.0]（与 A 反方向）");
        System.out.println();

        double simAB = cosineSimilarity(vectorA, vectorB);
        double simAC = cosineSimilarity(vectorA, vectorC);
        double simAD = cosineSimilarity(vectorA, vectorD);

        System.out.println("A 与 B 的余弦相似度: " + simAB);  // 1.0
        System.out.println("A 与 C 的余弦相似度: " + simAC);  // 0.267
        System.out.println("A 与 D 的余弦相似度: " + simAD);  // -1.0
    }

    /**
     * 计算两个向量的余弦相似度
     *
     * @param a 向量 A（浮点数数组）
     * @param b 向量 B（浮点数数组）
     * @return 余弦相似度，范围 [-1, 1]
     */
    public static double cosineSimilarity(float[] a, float[] b) {
        // 检查维度是否相同
        if (a.length != b.length) {
            throw new IllegalArgumentException("两个向量的维度必须相同");
        }

        double dotProduct = 0.0;   // 点积：A·B
        double normA = 0.0;        // 向量 A 的模长平方
        double normB = 0.0;        // 向量 B 的模长平方

        // 一次循环计算点积和模长平方
        for (int i = 0; i < a.length; i++) {
            dotProduct += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }

        // 避免除以 0
        if (normA == 0 || normB == 0) {
            return 0.0;
        }

        // 返回余弦相似度
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}