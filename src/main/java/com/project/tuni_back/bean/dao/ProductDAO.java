package com.project.tuni_back.bean.dao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.project.tuni_back.bean.vo.ProductVO;
import com.project.tuni_back.mapper.ProductMapper;

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
