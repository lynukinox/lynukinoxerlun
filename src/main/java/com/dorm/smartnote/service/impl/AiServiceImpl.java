package com.dorm.smartnote.service.impl;

import com.dorm.smartnote.service.AiService;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AiServiceImpl implements AiService {

    @Autowired
    private OpenAiChatModel chatModel; // Spring AI 自动注入

    @Override
    public String generateSummary(String content) {
        if (content == null || content.trim().isEmpty()) {
            return "笔记内容为空，无法生成摘要。";
        }

        // 构建提示词 (Prompt)，引导 AI 提取摘要
        String prompt = "你是一个智能笔记助手。请为以下笔记内容提取一个简短的摘要（50字以内）：\n\n" + content;

        try {
            // 调用大模型
            return chatModel.call(prompt);
        } catch (Exception e) {
            return "AI 暂时无法处理，请稍后重试。";
        }
    }
}