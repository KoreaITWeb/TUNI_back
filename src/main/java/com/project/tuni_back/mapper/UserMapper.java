package com.project.tuni_back.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.project.tuni_back.entity.User;

@Mapper // 이 인터페이스가 MyBatis 매퍼임을 나타냅니다.
public interface UserMapper {

    // User 객체를 받아 DB에 저장하는 메소드
    void save(User user);

    // 이메일로 사용자를 찾는 메소드 (추후 필요)
    User findByEmail(String email);
}