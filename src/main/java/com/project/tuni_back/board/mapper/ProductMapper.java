package com.project.tuni_back.board.mapper;

import com.project.tuni_back.board.bean.vo.ProductVO;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProductMapper {
    List<ProductVO> getAllProducts();
    ProductVO getProductById(int id);
    void insertProduct(ProductVO vo);
}
