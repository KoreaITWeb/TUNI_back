package com.project.tuni_back.controller;

import java.util.HashMap;
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

    // ✅ 수정: 메시지 전송 시 전역 사용자 알림도 함께 전송
    @MessageMapping("/chat/send") // 프론트 → /app/chat/send
    public void sendMessage(ChatMessageVO message) {
        // 1. DB에 메시지 저장
        chatService.saveMessage(message);


        
        // ✅ 3. 새로 추가: 채팅방 참여자들에게 전역 알림도 전송
        sendGlobalNotificationToParticipants(message);
    }
    
    // ✅ 새로 추가: 채팅방 참여자들에게 전역 알림 전송하는 헬퍼 메서드
    private void sendGlobalNotificationToParticipants(ChatMessageVO message) {
        try {
            // 채팅방 정보 조회해서 참여자 확인
            ChatRoomListVO chatRoom = chatService.getChatRoomById(message.getChatId());
            
            if (chatRoom != null) {
                // 전역 알림용 메시지 객체 생성
                Map<String, Object> globalNotification = new HashMap<>();
                globalNotification.put("chatId", message.getChatId());
                globalNotification.put("content", message.getContent());
                globalNotification.put("regdate", message.getRegdate());
                globalNotification.put("userId", message.getUserId());
                globalNotification.put("boardId", message.getBoardId());
                globalNotification.put("messageType", "chat"); // 메시지 타입 구분용
                
                // 구매자에게 전역 알림 전송
                if (chatRoom.getBuyerId() != null) {
                    messagingTemplate.convertAndSend(
                        "/topic/user/" + chatRoom.getBuyerId(), 
                        globalNotification
                    );
//                    System.out.println("🔍 전역 알림 전송 - 구매자: " + chatRoom.getBuyerId());
                }
                
                // 판매자에게 전역 알림 전송
                if (chatRoom.getSellerId() != null) {
                    messagingTemplate.convertAndSend(
                        "/topic/user/" + chatRoom.getSellerId(), 
                        globalNotification
                    );
//                    System.out.println("🔍 전역 알림 전송 - 판매자: " + chatRoom.getSellerId());
                }
            }
        } catch (Exception e) {
            System.err.println("전역 알림 전송 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // WebSocket을 통해 채팅방 생성 요청 처리 + 생성된 방을 구독자에게 브로드캐스트
    @MessageMapping("/createRoom") // 클라이언트가 /app/createRoom 으로 메시지 전송
    @SendTo("/topic/rooms") // 구독 중인 모든 사용자에게 /topic/rooms로 전송
    public ChatRoomListVO createRoom(ChatRoomListVO room) {
        chatService.createChatRoom(room); // 채팅방 생성 (chatId 자동 주입)
        return room; // 생성된 채팅방 정보 브로드캐스트
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