package com.project.tuni_back.board.service;
    
import com.project.tuni_back.board.bean.vo.BoardVO;
import com.project.tuni_back.board.mapper.BoardMapper;
import org.springframework.stereotype.Service;
import java.util.List;

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

