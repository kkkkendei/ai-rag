package indi.wzy.trigger.http;

import indi.wzy.api.IAIService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatClient;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;


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

    @PostConstruct
    public void init() {
        System.out.println("OllamaController已加载，API路径：/api/v1/ollama");
    }

    /**
     * http://localhost:8090/api/v1/ollama/generate?model=deepseek-r1:1.5b&message=1 plus 1
     */
    @RequestMapping(value = "/generate", method = RequestMethod.GET)
    @Override
    public ChatResponse generate(@RequestParam String model, @RequestParam String message) {
        return ollamaChatClient.call(new Prompt(message, OllamaOptions.create().withModel(model)));
    }

    /**
     * http://localhost:8090/api/v1/ollama/generateStream?model=deepseek-r1:1.5b&message=1 plus 1
     */
    @RequestMapping(value = "/generateStream", method = RequestMethod.GET)
    @Override
    public Flux<ChatResponse> generateStream(@RequestParam String model, @RequestParam String message) {
        return ollamaChatClient.stream(new Prompt(message, OllamaOptions.create().withModel(model)));
    }

}
