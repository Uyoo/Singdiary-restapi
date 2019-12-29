package com.singdiary.validator;

import com.singdiary.common.Description;
import com.singdiary.dto.Group;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

@Component
public class GroupValidator {

    @Description("그룹명이 중복되는지 확인")
    public void validator_duplicateName(Group group, Errors errors) throws Exception {
        if(group != null){
            errors.reject("그룹명 중복", "이미 존재하는 그룹입니다.");
        }
    }
}
