package com.dorm.smartnote.service;

public interface AiService {
    /**
     * 根据笔记内容生成智能摘要
     */
    String generateSummary(String content);
}