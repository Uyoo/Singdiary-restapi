package com.singdiary.linkResources;

import com.singdiary.dto.Group_List;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;

public class GroupResources_List extends EntityModel<Group_List> {

    public GroupResources_List(Group_List groupList, Link... links) {
        super(groupList, links);
    }
}
