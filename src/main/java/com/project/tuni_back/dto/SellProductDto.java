package com.project.tuni_back.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SellProductDto {
	private String title;
	private String price;
	private String content;
	private String userId;
	private Long schoolId;
}
