package com.project.tuni_back.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.project.tuni_back.bean.vo.ImageFileVO;

@Mapper
public interface ImageFileMapper {
   public int insert(ImageFileVO vo);
   public List<ImageFileVO> getImageFile(Long boardId);
   public int updateImageFile(ImageFileVO vo);
   public int deleteByBoardId(Long boardId);
   public int deleteByUuid(String uuid);
   public ImageFileVO findByUuid(String uuid);
   public void clearRepresentativeFlag(Long boardId);
   public void setRepresentativeFlag(String uuid);
}