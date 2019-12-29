package com.singdiary.dto.template_get;

import com.singdiary.common.Description;
import com.singdiary.linkResources.GroupResources_List;
import lombok.*;

@Description("/groups GET 형태로 요청이 들어왔을 때 응답 템플릿")
@AllArgsConstructor @NoArgsConstructor
@Getter @Setter
public class QueryGroup {

    //각각의 (그룹 리스트 + 링크) & 해당 url에 대한 링크 정보
    private GroupResources_List contents;

    //페이지 형태
    private Paging pages;
}
