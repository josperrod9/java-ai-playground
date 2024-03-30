package org.vaadin.marcus.langchain4j;

import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.TokenWindowChatMemory;
import dev.langchain4j.model.Tokenizer;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.embedding.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.HuggingFaceTokenizer;
import dev.langchain4j.model.localai.LocalAiStreamingChatModel;
import dev.langchain4j.model.mistralai.MistralAiChatModelName;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.time.Duration;

import static dev.langchain4j.data.document.loader.FileSystemDocumentLoader.loadDocument;

@Configuration
public class LangChain4jConfig {

    private static final String MODEL_NAME = MistralAiChatModelName.OPEN_MISTRAL_7B.toString();

    @Bean
    EmbeddingModel embeddingModel() {
        return new AllMiniLmL6V2EmbeddingModel();
    }

    @Bean
    EmbeddingStore<TextSegment> embeddingStore() {
        return new InMemoryEmbeddingStore<>();
    }

    @Bean
    Tokenizer tokenizer() {
        return new HuggingFaceTokenizer();
    }


    // In the real world, ingesting documents would often happen separately, on a CI server or similar
    @Bean
    CommandLineRunner docsToEmbeddings(
            EmbeddingModel embeddingModel,
            EmbeddingStore<TextSegment> embeddingStore,
            Tokenizer tokenizer,
            ResourceLoader resourceLoader
    ) throws IOException {
        return args -> {
            Resource resource =
                    resourceLoader.getResource("classpath:terms-of-service.txt");
            var termsOfUse = loadDocument(resource.getFile().toPath(), new TextDocumentParser());

            DocumentSplitter documentSplitter = DocumentSplitters.recursive(200, 0,
                    tokenizer);

            EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                    .documentSplitter(documentSplitter)
                    .embeddingModel(embeddingModel)
                    .embeddingStore(embeddingStore)
                    .build();
            ingestor.ingest(termsOfUse);
        };
    }

    @Bean
    StreamingChatLanguageModel chatLanguageModel(@Value("${model.url}") String url) {
        return LocalAiStreamingChatModel.builder()
                .baseUrl(url)
                .modelName(MODEL_NAME)
                .temperature(0.2)
                .timeout(Duration.ofMinutes(5))
                .build();
    }


    @Bean
    ContentRetriever retriever(
            EmbeddingStore<TextSegment> embeddingStore,
            EmbeddingModel embeddingModel
    ) {
        return EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(2)
                .minScore(0.6)
                .build();
    }


    @Bean
    CustomerSupportAgent customerSupportAgent(
            StreamingChatLanguageModel chatLanguageModel,
            Tokenizer tokenizer,
            ContentRetriever retriever,
            BookingTools tools
    ) {

        return AiServices.builder(CustomerSupportAgent.class)
                .streamingChatLanguageModel(chatLanguageModel)
                .chatMemoryProvider(chatId -> TokenWindowChatMemory.builder()
                        .id(chatId)
                        .maxTokens(2000, tokenizer)
                        .build())
                .contentRetriever(retriever)
                .tools(tools)
                .build();
    }
}
