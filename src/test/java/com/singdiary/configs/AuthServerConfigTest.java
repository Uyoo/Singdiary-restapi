package com.singdiary.configs;

import com.singdiary.common.BaseControllerTest;
import com.singdiary.common.TestDescription;
import com.singdiary.dto.Account;
import com.singdiary.service.AccountService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthServerConfigTest extends BaseControllerTest {

    @Autowired
    AccountService accountService;

    @Test
    @TestDescription("인증 토큰을 발급 받는 테스트")
    public void getAuthToken() throws Exception{
        //given
        String username = "uyoo2795";
        String password = "pass";

        String clientId = "myApp";
        String clientSecret = "pass";
        this.mockMvc.perform(post("/oauth/token")
                            //basic auth라는 헤더를 만듦
                            .with(httpBasic(clientId, clientSecret))
                            .param("username", username)
                            .param("password", password)
                            .param("grant_type", "password")
                        )
                        .andDo(print())
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("access_token").exists())
        ;
    }

    @Test
    @TestDescription("인증 토큰을 발급 받는 테스트 회원이 아닌 경우")
    public void getAuthToken_NotOurMember() throws Exception{
        //given
        String username = "uyoo22";
        String password = "test1";

        String clientId = "myApp";
        String clientSecret = "pass";
        this.mockMvc.perform(post("/oauth/token")
                //basic auth라는 헤더를 만듦
                .with(httpBasic(clientId, clientSecret))
                .param("username", username)
                .param("password", password)
                .param("grant_type", "password")
        )
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("error").exists())
        ;
    }

}