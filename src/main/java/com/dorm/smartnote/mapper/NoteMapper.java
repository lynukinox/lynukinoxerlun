package com.dorm.smartnote.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dorm.smartnote.entity.Note;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 笔记 Mapper 接口
 * 继承 BaseMapper 即可获得常用的 CRUD 能力
 */
@Mapper
public interface NoteMapper extends BaseMapper<Note> {
    // 手动定义一个查询，名字随便起
    @Select("SELECT * FROM note WHERE id = #{id}")
    Note selectByIdIncludingDeleted(Long id);

    // 1. 获取回收站
    @org.apache.ibatis.annotations.Select("SELECT * FROM note WHERE user_id = #{userId} AND deleted = 1 ORDER BY update_time DESC")
    List<Note> selectTrashNotes(Long userId);

    // 2. 还原笔记
    @org.apache.ibatis.annotations.Update("UPDATE note SET deleted = 0 WHERE id = #{id} AND user_id = #{userId}")
    int restoreNoteById(Long id, Long userId);
}