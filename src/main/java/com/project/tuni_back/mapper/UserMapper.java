package com.project.tuni_back.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.project.tuni_back.bean.vo.UserVO;

import io.lettuce.core.dynamic.annotation.Param;

@Mapper // 이 인터페이스가 MyBatis 매퍼임을 나타냅니다.
public interface UserMapper {

    // User 객체를 받아 DB에 저장하는 메소드
    void save(UserVO user);

    // 이메일로 사용자를 찾는 메소드
    UserVO findByEmail(String email);

    // 닉네임으로 사용자 찾는 메소드
    UserVO findByUserId(String userId);
    
    void updateUserId(@Param("oldUserId") String oldUserId, @Param("newUserId") String newUserId);

}