package com.project.tuni_back.bean.dao;

import org.springframework.stereotype.Repository;

import com.project.tuni_back.mapper.MainPageMapper;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class MainPageDAO {

    private final MainPageMapper mainPageMapper; // 변수명도 통일

    public int getProductCount() {
        return mainPageMapper.getProductCount();
    }

    public int getUserCount() {
        return mainPageMapper.getUserCount();
    }
}

