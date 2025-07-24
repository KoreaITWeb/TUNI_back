package com.project.tuni_back.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.project.tuni_back.bean.vo.ImageFileVO;

@Mapper
public interface ImageFileMapper {
   public void insert(ImageFileVO vo);
   public List<ImageFileVO> getImageFile(Long boardId);
}