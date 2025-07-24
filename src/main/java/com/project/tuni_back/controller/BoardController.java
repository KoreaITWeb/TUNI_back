package com.project.tuni_back.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.tuni_back.bean.vo.BoardVO;
import com.project.tuni_back.service.BoardService;

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
