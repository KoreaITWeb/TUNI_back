package com.project.tuni_back.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.project.tuni_back.bean.vo.ChatMessageVO;
import com.project.tuni_back.bean.vo.ChatRoomListVO;
import com.project.tuni_back.mapper.ChatMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMapper chatMapper;
    
 // ChatService.java에 추가
    public ChatRoomListVO getChatRoomById(Long chatId) {
        // 채팅방 ID로 채팅방 정보 조회 (buyerId, sellerId 포함)
        return chatMapper.getChatRoomById(chatId);
    }

    public void saveMessage(ChatMessageVO message) {
        chatMapper.registerChatMessage(message);
    }

    public List<ChatMessageVO> getMessages(Long chatId) {
        return chatMapper.getMessagesByChatId(chatId);
    }

    public List<ChatRoomListVO> getChatRoomsByUserId(String userId) {
        return chatMapper.getChatRoomsByUserId(userId);
    }

    public void createChatRoom(ChatRoomListVO vo) {
        chatMapper.registerChatRoom(vo);
    }
    
    public void updateChatRoom(String userId, Long chatId) {
    	chatMapper.quitChatRoom(userId, chatId);
    }
}
