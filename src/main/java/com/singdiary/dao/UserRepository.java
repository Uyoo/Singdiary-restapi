package com.singdiary.dao;

import com.singdiary.common.Description;
import com.singdiary.dto.Account;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository {

    /*====== /oauth/tokens ======*/
    @Description("account 테이블에서 아이디와 일치하는 사용자 정보 조회")
    Account findByUsername(String username);


    /*====== users/duplicate/{username} ======*/
    @Description("account 테이블에서 중복되는 닉네임의 정보 조회")
    List<Account> findDuplicateName(String username) throws Exception;

    /*====== users/ ======*/
    @Description("account 테이블에 사용자 정보 삽입")
    void insertUser(Account account) throws Exception;

    @Description("account 테이블에 사용자 정보 수정 - 프로필, 배경 이미지")
    void updateUserInfo(Account account) throws Exception;
}
