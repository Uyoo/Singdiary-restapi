package com.singdiary.linkResources;

import com.singdiary.dto.Mydiary;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;

public class MydiaryResources extends EntityModel<Mydiary> {

    public MydiaryResources(Mydiary mydiary, Link... links) {
        super(mydiary, links);
    }
}
