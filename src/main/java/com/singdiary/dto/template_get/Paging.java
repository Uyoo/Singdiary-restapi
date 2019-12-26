package com.singdiary.dto.template_get;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Paging {

    private Long totalElements;        //데이터의 총 개수
    private Integer totalPages;        //총 페이지
    private Integer pageSize;          //한 페이지에 얼만큼 데이터를 보고 싶은지
    private Integer pageNum;           //현재 페이지 번호
}
