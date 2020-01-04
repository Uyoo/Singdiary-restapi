package com.singdiary.linkResources;

import com.singdiary.dto.GroupDiary_List;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;

public class GroupDiaryResources_List extends EntityModel<GroupDiary_List> {

    public GroupDiaryResources_List(GroupDiary_List groupDiaryList, Link... links) {
        super(groupDiaryList, links);
    }
}
