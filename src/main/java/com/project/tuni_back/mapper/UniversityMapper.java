package com.project.tuni_back.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.project.tuni_back.bean.vo.UniversityVO;

@Mapper
public interface UniversityMapper {

    // 도메인 이름으로 대학교 정보를 찾는 메소드
    UniversityVO findByDomain(String domain);
    
    UniversityVO findById(Long id);
    List<UniversityVO> findAll();
    List<String> findDomainsByUniversityId(Long schoolId);

}