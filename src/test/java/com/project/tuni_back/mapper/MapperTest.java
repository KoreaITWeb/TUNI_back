package com.project.tuni_back.mapper;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.project.tuni_back.bean.vo.UniversityVO;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
public class MapperTest {

	@Autowired
	public UniversityMapper universityMapper;

	@Test
	void testUniversity() {
		List<UniversityVO> universities = universityMapper.findAll();
		System.out.println("--- 조회된 대학교 목록 (상위 5개) ---");
		universities.stream()
        .limit(5)
        .forEach(uni -> System.out.println(
                "ID: " + uni.getSchoolId() + ", Name: " + uni.getName()
        ));
		System.out.println("------------------------------------");
	}
}
