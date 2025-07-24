package com.project.tuni_back.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.project.tuni_back.bean.vo.ProductVO;

@Mapper
public interface ProductMapper {
    List<ProductVO> getAllProducts();
    ProductVO getProductById(int id);
    void insertProduct(ProductVO vo);
}