package com.project.tuni_back.board.controller;

import com.project.tuni_back.board.dao.ProductDAO;
import com.project.tuni_back.board.bean.vo.ProductVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private ProductDAO dao;

    @GetMapping("/register")
    public String showForm() {
        return "product/register";  // templates/product/register.html
    }

    @PostMapping("/register")
    public String register(ProductVO vo) {
        dao.insert(vo);
        return "redirect:/product/list";
    }

    @GetMapping("/list")
    public String list(Model model) {
        model.addAttribute("products", dao.getAll());
        return "product/list";  // templates/product/list.html
    }
}
//
