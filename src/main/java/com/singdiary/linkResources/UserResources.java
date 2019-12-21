package com.singdiary.linkResources;

import com.singdiary.dto.AccountDto;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;

public class UserResources extends EntityModel<AccountDto> {

    public UserResources(AccountDto accountDto, Link... links) {
        super(accountDto, links);
    }
}
