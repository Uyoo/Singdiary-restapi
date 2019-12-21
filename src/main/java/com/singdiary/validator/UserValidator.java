package com.singdiary.validator;

import com.singdiary.common.Description;
import com.singdiary.dto.Account;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import java.util.List;

@Component
public class UserValidator {

    @Description("닉네임이 중복하는지 판별")
    public void validator_duplicate(List<Account> duplicateAccounts, Errors errors){
        if(duplicateAccounts.size() > 0){
            errors.reject("닉네임 중복", "이미 존재하는 사용자 입니다.");
        }
    }

    @Description("요청을 보낸 사용자와 userId가 일치하는지")
    public void validator_matchUser(Account account, Integer userId, Errors errors) {
        if(account.getId() != userId){
            errors.reject("사용자가 불일치", "마이 다이어리에 대한 권한이 없는 사용자입니다.");
        }
    }

}
