package com.project.tuni_back.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.tuni_back.bean.vo.ChatMessageVO;
import com.project.tuni_back.bean.vo.ChatRoomListVO;
import com.project.tuni_back.service.ChatService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/room")
    public ResponseEntity<Void> createRoom(@RequestBody ChatRoomListVO vo) {
        chatService.createChatRoom(vo);
        return ResponseEntity.ok().build();
    }

    // ✅ 유저의 채팅방 리스트 (userId를 RequestBody로 받음)
    @PostMapping("/rooms")
    public ResponseEntity<List<ChatRoomListVO>> getRooms(@RequestBody Map<String, String> body) {
        String userId = body.get("userId");
        return ResponseEntity.ok(chatService.getChatRoomsByUserId(userId));
    }

    // ✅ 채팅방 메시지 내역 (chatId를 RequestBody로 받음)
    @PostMapping("/messages")
    public ResponseEntity<List<ChatMessageVO>> getMessages(@RequestBody Map<String, Long> body) {
        Long chatId = body.get("chatId");
        return ResponseEntity.ok(chatService.getMessages(chatId));
    }

    // ✅ 채팅방 나가기 (userId, chatId 모두 RequestBody로 받음)
    @PostMapping("/quit")
    public ResponseEntity<Void> quitChatRoom(@RequestBody Map<String, Object> body) {
        String userId = (String) body.get("userId");
        Long chatId = Long.valueOf(body.get("chatId").toString());
        chatService.updateChatRoom(userId, chatId);
        return ResponseEntity.ok().build();
    }

}
