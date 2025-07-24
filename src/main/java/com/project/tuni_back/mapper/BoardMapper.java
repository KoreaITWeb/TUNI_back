package com.project.tuni_back.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.project.tuni_back.bean.vo.BoardVO;

@Mapper
public interface BoardMapper {
    List<BoardVO> findAll();
    BoardVO findById(Long id);
    void insert(BoardVO product);
}