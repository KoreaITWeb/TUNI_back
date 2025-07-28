package com.project.tuni_back.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.project.tuni_back.bean.vo.BoardVO;
import com.project.tuni_back.mapper.BoardMapper;
import com.project.tuni_back.mapper.UserMapper;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
@RequestMapping("/board/*")
public class BoardController {
	@Autowired
	private BoardMapper mapper;
	
	@Autowired
	private UserMapper umapper;
	
	@GetMapping("register")
	public void register2(Model model, String userId) {
		model.addAttribute("user", umapper.findByNickname(userId));
	}
	
	@PostMapping("register")
	public String register(BoardVO vo,String userId,Model model, RedirectAttributes rttr) {
		model.addAttribute("user", umapper.findByNickname(userId));
		if(mapper.registerProduct(vo) > 0) {
			log.info(vo.getUserId() + "님이 글을 등록함");
			
		}
		rttr.addAttribute("id", umapper.findByNickname(userId).getUser_id());
		rttr.addAttribute("userId", userId);
		return "redirect:/board/list";
	}
	
	
    
	
	@GetMapping("list")
	public void list(Long schoolId,String userId, Model model){
		log.info("List on");
		model.addAttribute("user", umapper.findByNickname(userId));
		model.addAttribute("list", mapper.getProductList(schoolId));
	}
	@GetMapping("read")
	public void read(Long boardId, String userId, Model model) {
		log.info("read on");
		log.info("read : " + mapper.readProduct(boardId));
		model.addAttribute("user", umapper.findByNickname(userId));
		model.addAttribute("read", mapper.readProduct(boardId));
	}
}
