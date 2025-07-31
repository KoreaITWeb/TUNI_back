package com.project.tuni_back.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    // 채팅방 생성
    @PostMapping("/room")
    public ResponseEntity<Void> createRoom(@RequestBody ChatRoomListVO vo) {
        chatService.createChatRoom(vo);
        return ResponseEntity.ok().build();
    }

    // 유저의 채팅방 리스트
    @GetMapping("/rooms")
    public ResponseEntity<List<ChatRoomListVO>> getRooms(@RequestParam String userId) {
        return ResponseEntity.ok(chatService.getChatRoomsByUserId(userId));
    }

    // 채팅방 메시지 내역
    @GetMapping("/messages")
    public ResponseEntity<List<ChatMessageVO>> getMessages(@RequestParam Long chatId) {
        return ResponseEntity.ok(chatService.getMessages(chatId));
    }
    
    // 채팅방 나가기
    @PostMapping("/quit")
    public ResponseEntity<Void> quitChatRoom(@RequestParam String userId, @RequestParam Long chatId){
    	chatService.updateChatRoom(userId, chatId);
    	return ResponseEntity.ok().build();
    }
}
