package com.project.tuni_back.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.project.tuni_back.bean.vo.BoardVO;

import io.lettuce.core.dynamic.annotation.Param;
@Mapper
public interface BoardMapper {
	public List<BoardVO> getProductList(Long schoolId);
	public List<BoardVO> getProductList(String userId);
	public List<BoardVO> findBoardsById(@Param("boardId") List<Long> boardId);
	public BoardVO readProduct(Long boardId);
	public int registerProduct(BoardVO vo);
	public int removeProduct(Long boardId);
	public int updateProduct(BoardVO vo);
	int updateStatus(Long boardId, String saleStatus);
	public int updateViews(Long boardId);
	public int incrementLikes(Long boardId);
	public int decrementLikes(Long boardId);
	int resetViews(Long boardId);
}