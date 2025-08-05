package com.project.tuni_back.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.tuni_back.bean.dao.MainPageDAO;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/main")  // ← stats → main 으로 통일
public class MainPageController {

    private final MainPageDAO mainPageDAO;

    @GetMapping("/counts")
    public Map<String, Integer> getCounts() {
        int productCount = mainPageDAO.getProductCount();
        int userCount = mainPageDAO.getUserCount();

        Map<String, Integer> result = new HashMap<>();
        result.put("productCount", productCount);
        result.put("userCount", userCount);
        return result;
    }
}


