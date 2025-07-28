package com.project.tuni_back.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.project.tuni_back.bean.vo.ImageFileVO;

@Mapper
public interface ImageFileMapper {
   public int insert(ImageFileVO vo);
   public List<ImageFileVO> getImageFile(Long boardId);
   public ImageFileVO getImageByUuid(String uuid);
   public int deleteByUuid(String uuid);
}