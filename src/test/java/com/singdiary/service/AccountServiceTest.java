package com.singdiary.service;

import com.singdiary.common.AppProperties;
import com.singdiary.common.TestDescription;
import com.singdiary.dao.UserRepository;
import com.singdiary.dto.Account;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;


@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class AccountServiceTest {

    @Autowired
    AccountService accountService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    AppProperties appProperties;

    @Test
    @TestDescription("해당 아이디가 우리 서비스 회원이라면 정상 조회")
    public void findByUsername() throws Exception {
        Account account = Account.builder()
                    .name(appProperties.getUserUsername())
                    .password(appProperties.getUserPassword())
                    .build();
        account.init_images();
        account.setRole("USER");
//        this.accountService.insertUser(account);

        //when
        UserDetails userDetails = accountService.loadUserByUsername(account.getName());

        //then
        assertThat(passwordEncoder.matches(account.getPassword(), userDetails.getPassword())).isTrue();
    }

    @Test
    @TestDescription("해당 아이디가 우리 서비스 회원이 아니라면")
    public void findByUsername_Fail() {
        //given
        String username = "uyoo22";
        UserDetails userDetails = accountService.loadUserByUsername(username);
//        assertThat(userDetails).

        /*try {
            UserDetails userDetails = accountService.loadUserByUsername(username);
            fail("supposed to be failed");
        } catch (UsernameNotFoundException e){
            assertThat(e).isEqualTo(userDetails);
        }*/
    }
}