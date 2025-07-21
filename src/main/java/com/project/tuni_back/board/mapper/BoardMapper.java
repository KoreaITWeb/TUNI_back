package com.project.tuni_back.board.mapper;

import com.project.tuni_back.board.bean.vo.BoardVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface BoardMapper {
    List<BoardVO> findAll();
    BoardVO findById(Long id);
    void insert(BoardVO product);
}