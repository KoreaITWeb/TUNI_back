package com.project.tuni_back.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.project.tuni_back.bean.vo.BoardVO;
import com.project.tuni_back.mapper.BoardMapper;

@Service
public class BoardService {
    private final BoardMapper productMapper;

    public BoardService(BoardMapper productMapper) {
        this.productMapper = productMapper;
    }

    public List<BoardVO> getAllBoardVO() {
        return productMapper.findAll();
    }

    public BoardVO getBoardVO(Long id) {
        return productMapper.findById(id);
    }

    public void createBoardVO(BoardVO product) {
        productMapper.insert(product);
    }
}
