package com.project.tuni_back.controller;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class TestController {
	
	@GetMapping("/index") // 
	public String getIndex() {
		return "index"; // "src/main/resources/templates/index.html"을 찾아감
	}
}
