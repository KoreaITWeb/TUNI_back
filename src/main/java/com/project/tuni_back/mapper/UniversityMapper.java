package com.project.tuni_back.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.project.tuni_back.entity.University;

@Mapper
public interface UniversityMapper {

    // 도메인 이름으로 대학교 정보를 찾는 메소드
    University findByDomain(String domain);
    
    University findById(Long id);
    List<University> findAll();
    List<String> findDomainsByUniversityId(Long schoolId);

}