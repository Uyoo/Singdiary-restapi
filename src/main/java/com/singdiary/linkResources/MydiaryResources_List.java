package com.singdiary.linkResources;

import com.singdiary.dto.Mydiary_List;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;

public class MydiaryResources_List extends EntityModel<Mydiary_List> {

    public MydiaryResources_List(Mydiary_List mydiaryList, Link... links) {
        super(mydiaryList, links);
    }
}
