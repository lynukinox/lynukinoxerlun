package com.dorm.smartnote.controller;

import com.dorm.smartnote.entity.Note;
import com.dorm.smartnote.mapper.NoteMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ai")
public class AiController {

    private final ChatClient chatClient;
    private final NoteMapper noteMapper;

    // 重点：通过 @Value 强行抓取 yml 里的配置项
    public AiController(NoteMapper noteMapper) { // 删掉那些 @Value 参数
        this.noteMapper = noteMapper;

        // 直接在这里贴上你的 Key 和 URL，不走 yml 注入
        String apiKey = "sk-OGhbpSKtnFNCFmc1hLCD2c4BD6YLEZ1DYpfBgPXz22thujJ4";
        String baseUrl = "https://api.chatanywhere.tech";

        System.out.println("--- 物理硬编码初始化检查 ---");
        System.out.println("API Key: " + apiKey);

        OpenAiApi openAiApi = new OpenAiApi(baseUrl, apiKey);
        OpenAiChatModel chatModel = new OpenAiChatModel(openAiApi);
        this.chatClient = ChatClient.builder(chatModel).build();
    }
    @GetMapping("/chat")
    public String chat(@RequestParam(value = "message") String message) {
        try {
            String aiResponse = chatClient.prompt()
                    .user(message)
                    .call()
                    .content();

            Note note = new Note();
            note.setTitle("AI对话: " + (message.length() > 10 ? message.substring(0, 10) : message));
            note.setContent(message);
            note.setSummary(aiResponse);
            note.setUserId(1L);
            noteMapper.insert(note);

            return aiResponse;
        } catch (Exception e) {
            e.printStackTrace();
            return "AI调用失败: " + e.getMessage();
        }
    }
}