package com.project.tuni_back.bean.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.project.tuni_back.mapper.ViewTrackingMapper;

@Repository
public class ViewTrackingDAO {
	@Autowired
	private ViewTrackingMapper mapper;
	
	
}
