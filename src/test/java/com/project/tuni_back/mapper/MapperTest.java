package com.project.tuni_back.mapper;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.project.tuni_back.bean.vo.ImageFileVO;
import com.project.tuni_back.bean.vo.UniversityVO;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
public class MapperTest {
	@Autowired
	private ImageFileMapper fmapper;
	
//	@Test
	public void fileTest() {
		ImageFileVO vo = new ImageFileVO();
		vo.setBoardId(1L);
		vo.setFileName("asdfs");
		vo.setUploadPath("c://wow");
		vo.setUuid("3f28d9fh2h");
		
		if(fmapper.insert(vo)>0) {
			log.info("insertTest: success");
		}
		else {
			log.info("insert fail");
		}
	}
	

	@Autowired
	public UniversityMapper universityMapper;

//	@Test
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
