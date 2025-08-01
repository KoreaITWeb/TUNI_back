package com.project.tuni_back.bean.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.project.tuni_back.bean.vo.ViewTrackingVO;
import com.project.tuni_back.mapper.BoardMapper;
import com.project.tuni_back.mapper.ViewTrackingMapper;

@Repository
public class ViewTrackingDAO {
	@Autowired
	private ViewTrackingMapper vmapper;
	
	@Autowired
	private BoardMapper bmapper;
	
	public void views(ViewTrackingVO vo) {
		if(vmapper.readViews(vo.getUserId(), vo.getBoardId())<1) {
			vmapper.registerViews(vo);
			bmapper.updateViews(vo.getBoardId());
		}
	}
	
}
