package com.project.tuni_back.mapper;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MainPageMapper {
    int getProductCount();
    int getUserCount();
}

