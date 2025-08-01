package com.project.tuni_back.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.tuni_back.bean.dao.ViewTrackingDAO;
import com.project.tuni_back.bean.vo.ViewTrackingVO;

@RestController
@RequestMapping("/views/*")
public class ViewTrackingController {
	@Autowired
	private ViewTrackingDAO dao;
	
	@PostMapping("tracking")
	public ResponseEntity<Void> tracking(@RequestBody ViewTrackingVO vo) {
		dao.views(vo);
		return ResponseEntity.ok().build();
	}
	
}
