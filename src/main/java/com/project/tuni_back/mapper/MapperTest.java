package com.project.tuni_back.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface MapperTest {
	@Select("SELECT SYSDATE() FROM DUAL")
	public String getTime1();
	
	public String getTime2();
	
	public String getTime3();
}
