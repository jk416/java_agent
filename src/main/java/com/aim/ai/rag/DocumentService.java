package com.aim.ai.rag;

import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;

@Service
public class DocumentService {

    private static final TokenTextSplitter SPLITTER = new TokenTextSplitter();


    public List<Document> splitDocument(String content) {
        if (!StringUtils.hasText(content)) {
            return Collections.emptyList();
        }
        Document rawDocument = new Document(content);
        return SPLITTER.apply(List.of(rawDocument));
    }


}
