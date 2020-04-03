package com.singdiary.service;

import com.singdiary.common.Description;
import com.singdiary.dao.UserRepository;
import com.singdiary.dto.Account;
import com.singdiary.dto.AccountAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccountService implements UserDetailsService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Override
    @Description("해당 사용자가 우리 서비스 회원인지 확인")
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = this.userRepository.findByUsername(username);

        if(account == null){
            UsernameNotFoundException notFoundException = new UsernameNotFoundException(username);
            notFoundException.printStackTrace();    //사용자가 없다면 에러 메시지 출력

            return null;
        }

        return new AccountAdapter(account);
    }


    @Description("아이디를 통해 기존 회원인지 확인")
    public Account findByUsername(String username) throws Exception {
        return this.userRepository.findByUsername(username);
    }

    @Description("사용자 닉네임과 중복되는 사용자 정보 조회")
    public List<Account> findDuplicateName(String username) throws Exception {
        return userRepository.findDuplicateName(username);
    }

    @Description("사용자 정보 삽입")
    public void insertUser(Account account) throws Exception{
        //패스워드 인코딩
        account.setPassword(passwordEncoder.encode(account.getPassword()));
        userRepository.insertUser(account);
    }

    @Description("사용자 정보 수정 - 프로필, 배경 이미지")
    public void updateUserInfo(Account account) throws Exception {
        userRepository.updateUserInfo(account);
    }
}
