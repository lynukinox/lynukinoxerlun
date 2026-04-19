package com.dorm.smartnote.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dorm.smartnote.entity.Note;
import com.dorm.smartnote.mapper.NoteMapper;
import com.dorm.smartnote.service.NoteService;
import org.springframework.stereotype.Service; // 必须有这个导入

@Service // 必须有这个注解！
public class NoteServiceImpl extends ServiceImpl<NoteMapper, Note> implements NoteService {
}