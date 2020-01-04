package com.singdiary.linkResources;

import com.singdiary.dto.GroupDiary;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;

public class GroupDiaryResources extends EntityModel<GroupDiary> {

    public GroupDiaryResources(GroupDiary groupDiary, Link... links) {
        super(groupDiary, links);
    }
}
