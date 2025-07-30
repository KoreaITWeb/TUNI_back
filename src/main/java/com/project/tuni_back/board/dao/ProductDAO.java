package com.project.tuni_back.board.dao;

import com.project.tuni_back.board.mapper.ProductMapper;
import com.project.tuni_back.board.bean.vo.ProductVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ProductDAO {

    @Autowired
    ProductMapper mapper;

    public List<ProductVO> getAll() {
        return mapper.getAllProducts();
    }

    public ProductVO getById(int id) {
        return mapper.getProductById(id);
    }

    public void insert(ProductVO vo) {
        mapper.insertProduct(vo);
    }
}
//
