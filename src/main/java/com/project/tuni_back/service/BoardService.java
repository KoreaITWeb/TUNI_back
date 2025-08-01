package com.project.tuni_back.service;

import java.nio.file.AccessDeniedException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.tuni_back.bean.vo.BoardVO;
import com.project.tuni_back.bean.vo.ImageFileVO;
import com.project.tuni_back.bean.vo.LikesVO;
import com.project.tuni_back.bean.vo.UserVO;
import com.project.tuni_back.dto.SellProductDto;
import com.project.tuni_back.mapper.BoardMapper;
import com.project.tuni_back.mapper.ImageFileMapper;
import com.project.tuni_back.mapper.LikesMapper;
import com.project.tuni_back.mapper.UserMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardMapper boardMapper;
    private final UserMapper userMapper;
    private final ImageFileMapper imageFileMapper;
    private final LikesMapper likesMapper;

    /**
     * 게시글 등록 페이지에 필요한 사용자 정보를 조회합니다.
     * @param userId 사용자 ID
     * @return 사용자 정보
     */
    public UserVO getRegisterInfo(String userId) {
        return userMapper.findByUserId(userId);
    }

    /**
     * 새로운 게시글을 등록합니다.
     * @param dto 게시글 정보 DTO
     * @return 생성된 BoardVO 객체
     */
    @Transactional
    public BoardVO registerProduct(SellProductDto dto) {
        BoardVO vo = new BoardVO();
        vo.setUserId(dto.getUserId());
        vo.setSchoolId(dto.getSchoolId());
        vo.setTitle(dto.getTitle());
        vo.setContent(dto.getContent());
        vo.setPrice(dto.getPrice());
        vo.setCategory(dto.getCategory());

        boardMapper.registerProduct(vo);
        return vo;
    }

    /**
     * 게시글 목록과 사용자 정보를 함께 조회합니다.
     * @param schoolId 학교 ID
     * @param userId 사용자 ID
     * @return 게시글 목록과 사용자 정보를 담은 Map
     */
    public Map<String, Object> getProductList(Long schoolId, String userId) {
        UserVO user = userMapper.findByUserId(userId);
        List<BoardVO> list = boardMapper.getProductList(schoolId);

        Map<String, Object> response = new HashMap<>();
        response.put("user", user);
        response.put("list", list);
        return response;
    }

    /**
     * 게시글 상세 정보(게시글, 작성자, 이미지)를 조회합니다.
     * @param boardId 게시글 ID
     * @return 상세 정보를 담은 Map
     */
    public Map<String, Object> getProductDetails(Long boardId, String userId) {
        BoardVO board = boardMapper.readProduct(boardId);
        if (board == null) {
            throw new IllegalArgumentException("존재하지 않는 게시물입니다.");
        }
        // UserVO user = userMapper.findByUserId(userId);
        List<ImageFileVO> images = imageFileMapper.getImageFile(boardId);
        
        boolean isLikedByUser = false;
        if (userId != null) {
            // 로그인한 사용자일 경우에만 좋아요 여부 확인
            isLikedByUser = likesMapper.findByUserAndBoard(userId, boardId) != null;
        }

        Map<String, Object> response = new HashMap<>();
        response.put("board", board);
        // response.put("user", user);
        response.put("images", images);
        response.put("isLikedByUser", isLikedByUser);
        return response;
    }

    /**
     * 게시글을 수정합니다.
     * @param vo 수정할 게시글 정보
     * @param currentUserId 현재 로그인한 사용자 ID
     * @return 수정 성공 여부
     * @throws AccessDeniedException 소유자가 아닐 경우 발생
     */
    public boolean updateProduct(BoardVO vo, String currentUserId) throws AccessDeniedException {
        BoardVO board = boardMapper.readProduct(vo.getBoardId());
        if (board == null) {
            throw new IllegalArgumentException("존재하지 않는 게시물입니다.");
        }
        if (!board.getUserId().equals(currentUserId)) {
            throw new AccessDeniedException("이 게시물을 수정할 권한이 없습니다.");
        }
        return boardMapper.updateProduct(vo) > 0;
    }

    /**
     * 게시글을 삭제합니다. (소유자 확인 포함)
     * @param boardId 삭제할 게시글 ID
     * @param currentUserId 현재 로그인한 사용자 ID
     * @return 삭제 성공 여부
     * @throws AccessDeniedException 소유자가 아닐 경우 발생
     */
    public boolean removeProduct(Long boardId, String currentUserId) throws AccessDeniedException {
        BoardVO board = boardMapper.readProduct(boardId);
        if (board == null) {
            throw new IllegalArgumentException("존재하지 않는 게시물입니다.");
        }
        if (!board.getUserId().equals(currentUserId)) {
            throw new AccessDeniedException("이 게시물을 삭제할 권한이 없습니다.");
        }
        return boardMapper.removeProduct(boardId) > 0;
    }
    
    /**
     * 게시물의 판매 상태를 변경합니다. (소유자 확인 포함)
     * @param boardId 게시물 ID
     * @param saleStatus 새로운 판매 상태
     * @param currentUserId 현재 로그인한 사용자 ID
     * @return 변경 성공 여부
     * @throws AccessDeniedException 소유자가 아닐 경우 발생
     */
    public boolean updateBoardStatus(Long boardId, String saleStatus, String currentUserId) throws AccessDeniedException {
        // 1. 먼저 게시물 정보를 가져옵니다.
        BoardVO board = boardMapper.readProduct(boardId);
        if (board == null) {
            throw new IllegalArgumentException("존재하지 않는 게시물입니다.");
        }

        // 2. (중요) 현재 로그인한 사용자가 게시물 작성자인지 확인합니다.
        if (!board.getUserId().equals(currentUserId)) {
            throw new AccessDeniedException("이 게시물을 수정할 권한이 없습니다.");
        }

        // 3. 권한이 있다면 상태를 변경합니다.
        return boardMapper.updateStatus(boardId, saleStatus) > 0;
    }
    
    @Transactional
    public boolean toggleLike(Long boardId, String userId) {
        // LikesVO는 userId와 boardId를 필드로 가집니다.
        LikesVO existingLike = likesMapper.findByUserAndBoard(userId, boardId);

        if (existingLike != null) {
            // 이미 좋아요를 눌렀으면 -> 좋아요 취소
            likesMapper.delete(existingLike.getLikeId());
            boardMapper.decrementLikes(boardId);
            return false; // "좋아요 취소됨"을 의미
        } else {
            // 좋아요를 누르지 않았으면 -> 좋아요 추가
            LikesVO newLike = new LikesVO(userId, boardId);
            likesMapper.insert(newLike);
            boardMapper.incrementLikes(boardId);
            return true; // "좋아요 처리됨"을 의미
        }
    }
}
