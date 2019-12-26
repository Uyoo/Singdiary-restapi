package com.singdiary.dto.template_get;

import com.singdiary.common.Description;
import com.singdiary.linkResources.MydiaryResources_List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Description("/users/{id}/mydiary GET 형태로 요청이 들어왔을 때 응답 템플릿")
@AllArgsConstructor @NoArgsConstructor
@Getter @Setter
public class QueryUserMydiary {

    //해당 사용자가 마이 다이어리에 업로드한 각각의 (곡 리스트 + 링크) & 해당 url에 대한 링크 정보
    private MydiaryResources_List contents;

    //페이지 형태
    private Paging pages;
}
