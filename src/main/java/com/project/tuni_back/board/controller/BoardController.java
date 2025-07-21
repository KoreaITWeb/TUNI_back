package com.project.tuni_back.board.controller;

import com.project.tuni_back.board.bean.vo.BoardVO;
import com.project.tuni_back.board.service.BoardService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/board/*")
public class BoardController {
    private final BoardService productService;

    public BoardController(BoardService productService) {
        this.productService = productService;
    }

    // 상품 목록 조회
    @GetMapping("/{shop}")
    public List<BoardVO> getAllProducts() {
        return productService.getAllBoardVO();
    }

    // 상품 상세 조회
    @GetMapping("/{id}")
    public BoardVO getProduct(@PathVariable Long id) {
        return productService.getBoardVO(id);
    }

    // 상품 등록
    @PostMapping
    public void createProduct(@RequestBody BoardVO product) {
        productService.createBoardVO(product);
    }
}