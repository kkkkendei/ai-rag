package indi.wzy.trigger.http;

import indi.wzy.api.IAIService;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.ollama.OllamaChatClient;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.vectorstore.PgVectorStore;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * @author wuzeyu
 * @description
 * @github github.com/kkkkendei
 */
@RestController()
@CrossOrigin("*")
@RequestMapping("/api/v1/ollama")
public class OllamaController implements IAIService {

    @Resource
    private OllamaChatClient ollamaChatClient;

    @Resource
    private PgVectorStore pgVectorStore;


    /**
     * http://localhost:8090/api/v1/ollama/generate?model=deepseek-r1:1.5b&message=1 plus 1
     */
    @RequestMapping(value = "generate", method = RequestMethod.GET)
    @Override
    public ChatResponse generate(@RequestParam String model, @RequestParam String message) {
        return ollamaChatClient.call(new Prompt(message, OllamaOptions.create().withModel(model)));
    }

    /**
     * http://localhost:8090/api/v1/ollama/generateStream?model=deepseek-r1:1.5b&message=1 plus 1
     */
    @RequestMapping(value = "generateStream", method = RequestMethod.GET)
    @Override
    public Flux<ChatResponse> generateStream(@RequestParam String model, @RequestParam String message) {
        return ollamaChatClient.stream(new Prompt(message, OllamaOptions.create().withModel(model)));
    }

    @RequestMapping(value = "generate_stream_rag", method = RequestMethod.GET)
    @Override
    public Flux<ChatResponse> generateStreamRag(@RequestParam String model, @RequestParam String ragTag, @RequestParam String message) {
        String SYSTEM_PROMPT = """
                Use the information from the DOCUMENTS section to provide accurate answers but act as if you knew this information innately.
                If unsure, simply state that you don't know.
                Another thing you need to note is that your reply must be in Chinese!
                DOCUMENTS:
                    {documents}
                """;

        // 获取知识库中的文档
        SearchRequest request = SearchRequest.query(message)
                .withTopK(5)
                .withFilterExpression("knowledge == '" + ragTag + "'");

        List<Document> documents = pgVectorStore.similaritySearch(request);
        String documentsCollectors = documents.stream()
                .map(Document::getContent)
                .collect(Collectors.joining());
        Message ragMessage = new SystemPromptTemplate(SYSTEM_PROMPT).createMessage(Map.of("documents", documentsCollectors));

        List<Message> messages = new ArrayList<>();
        messages.add(new UserMessage(message));
        messages.add(ragMessage);

        return ollamaChatClient.stream(new Prompt(messages, OllamaOptions.create().withModel(model)));
    }

}
