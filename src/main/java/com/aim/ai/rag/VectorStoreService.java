package com.aim.ai.rag;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class VectorStoreService {


    private static final int BATCH_SIZE = 20;

    private final VectorStore vectorStore;

    // 1. 构造器注入 Spring AI 统一抽象的 VectorStore
    public VectorStoreService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    // 2. 向量化入库
    public void saveDocuments(List<Document> documents) {
        if (documents == null || documents.isEmpty()) {
            log.warn("Documents are null or empty");
            return;
        }
        for (int i = 0; i < documents.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, documents.size());
            List<Document> batch = documents.subList(i, end);
            vectorStore.add(batch);
            log.info("成功向量化写入一批文件 {}", batch.size());
        }
    }

    // 3. 语义相似度搜索
    public List<Document> searchSimilar(String query, int topK) {
        // 构建 SearchRequest，设置 query、topK，并调用 similaritySearch(...)
        SearchRequest request = SearchRequest.query(query).withTopK(topK);
        return vectorStore.similaritySearch(request);
    }


}
