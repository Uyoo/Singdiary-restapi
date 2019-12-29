package com.singdiary.linkResources;

import com.singdiary.dto.Group;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;

public class GroupResources extends EntityModel<Group> {

    public GroupResources(Group group, Link... links) {
        super(group, links);
    }
}
