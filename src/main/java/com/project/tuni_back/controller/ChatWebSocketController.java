package com.project.tuni_back.controller;

import java.util.Map;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.project.tuni_back.bean.vo.ChatMessageVO;
import com.project.tuni_back.bean.vo.ChatRoomListVO;
import com.project.tuni_back.service.ChatService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;

    @MessageMapping("/chat/send") // 프론트 → /app/chat/send
    public void sendMessage(ChatMessageVO message) {
        // 1. DB에 메시지 저장
        chatService.saveMessage(message);

        // 2. 상대방에게 메시지 전송 (구독자에게)
        messagingTemplate.convertAndSend("/topic/chat/" + message.getChatId(), message);
    }
    
 // WebSocket을 통해 채팅방 생성 요청 처리 + 생성된 방을 구독자에게 브로드캐스트
    @MessageMapping("/createRoom") // 클라이언트가 /app/createRoom 으로 메시지 전송
    @SendTo("/topic/rooms")        // 구독 중인 모든 사용자에게 /topic/rooms로 전송
    public ChatRoomListVO createRoom(ChatRoomListVO room) {
        chatService.createChatRoom(room); // 채팅방 생성 (chatId 자동 주입)
        return room;                      // 생성된 채팅방 정보 브로드캐스트
    }
    
    
    
    @MessageMapping("/chat/quit")
    @SendTo("/topic/rooms")
    public Map<String, Object> quitChatRoom(Map<String, Object> quitData) {
        String userId = (String) quitData.get("userId");
        Long chatId = Long.valueOf(quitData.get("chatId").toString());
        
        chatService.updateChatRoom(userId, chatId);
        
        // 클라이언트에서 구분할 수 있도록 action 추가
        quitData.put("action", "quit");
        return quitData;
    }



}
