package com.aliyun.ai;

import okhttp3.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.print.DocFlavor;
import java.io.IOException;

public class EmbeddingDemo {

    private static final String API_KEY=System.getenv("API_KEY");
    private static final String EMBEDDING_URL= "https://dashscope.aliyuncs.com/api/v1/services/embeddings/text-embedding/text-embedding";


    private static final OkHttpClient client=new OkHttpClient.Builder()
            .connectTimeout(30,java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(60,java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(30,java.util.concurrent.TimeUnit .SECONDS)
            .build();

    private static final ObjectMapper mapper=new ObjectMapper();


    public static void main(String[] args)throws IOException{
        if(API_KEY==null||API_KEY.isEmpty())
        {
            System.err.println("请设置环境变量 API_KEY");
            return;
        }

        String text="我喜欢吃苹果";
        float[] embedding =getEmbedding(text);

        System.out.println("文本："+ text);
        System.out.println("向量维度："+ embedding.length);
        System.out.println("前十个值：");
        for(int i=0;i<10;i++){
            System.out.print(embedding[i]+"  ");
        }
        System.out.println();
    }
    public static float[] getEmbedding(String text)throws IOException{
        String json= String.format("""
                {
                "model":"text-embedding-v3",
                "input":{
                "texts": ["%s"]},
               "parameters": {"text_type": "query" }               
                """,text);
        System.out.println("请求体: " + json);


       Request request =new Request.Builder()
               .url(EMBEDDING_URL)
               .header("Authorization","Bearer "+API_KEY)
               .header("Content-type","application/json")
               .post(RequestBody.create(json,MediaType.parse("application/json")))
               .build();
       try(Response response=client.newCall(request).execute())

    {
        if (!response.isSuccessful()) {
            throw new IOException("HTPP" + response.code());
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

}




