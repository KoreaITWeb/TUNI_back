package com.project.tuni_back.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.project.tuni_back.bean.vo.ChatMessageVO;
import com.project.tuni_back.bean.vo.ChatRoomListVO;

@Mapper
public interface ChatMapper {
	public int registerChatRoom(ChatRoomListVO vo);
	public int registerChatMessage(ChatMessageVO vo);
	public List<ChatRoomListVO> getChatRoomsByUserId(String userId);
	public List<ChatMessageVO> getMessagesByChatId(Long chatId);
	public int quitChatRoom(String userId, Long chatId);
}
