package com.singdiary.controller;

import com.singdiary.common.AppProperties;
import com.singdiary.common.BaseControllerTest;
import com.singdiary.common.TestDescription;
import com.singdiary.configs.AppConfig;
import com.singdiary.dao.UserRepository;
import com.singdiary.dto.Account;
import com.singdiary.service.AccountService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.common.util.Jackson2JsonParser;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerTest extends BaseControllerTest {

    @Autowired
    AccountService accountService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    AppProperties appProperties;

    //============= 로그인 서비스 관리 =============

    @Test
    @TestDescription("중복되는 닉네임 조회")
    public void findDuplicateUser() throws Exception {

        Account account = Account.builder()
                .name("uyoo")
                .password("test1")
                .build();

        this.mockMvc.perform(get("/users/duplicate/{username}", account.getName())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("message").exists())
        ;
    }

    @Test
    @TestDescription("닉네임이 중복하지 않는 경우")
    public void findDuplicateUser_Fail() throws Exception {
        Account account = Account.builder()
                .name("yja3806")
                .password("test2")
                .build();

        this.mockMvc.perform(get("/users/duplicate/{username}", account.getName())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
        )
                .andDo(print())
                .andExpect(status().isNotFound())
        ;
    }

    @Test
    @TestDescription("사용자 정보 추가 정상 처리")
    public void addUser() throws Exception {
        Account newAccount = Account.builder()
                .name(appProperties.getUserUsername())
                .password(appProperties.getUserPassword())
                .build();

        this.mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(newAccount))
        )
                .andDo(print())
                .andExpect(status().isCreated())
        ;
    }


    @Test
    @TestDescription("로그인한 회원 정보 조회")
    public void findOurServiceUser() throws Exception {
        this.mockMvc.perform(get("/users")
                .header(HttpHeaders.AUTHORIZATION, getBearerToken())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
        )
                .andDo(print())
                .andExpect(status().isOk())
        ;
    }


    private String getBearerToken() throws Exception {
        return "Bearer " + getAccessToken();
    }

    //accesstoken 받기
    private String getAccessToken() throws Exception {
        ResultActions perform = this.mockMvc.perform(post("/oauth/token")

                //basic auth라는 헤더를 만듦
                .with(httpBasic(appProperties.getClientId(), appProperties.getClientSecret()))
                .param("username", appProperties.getUserUsername())
                .param("password", appProperties.getUserPassword())
                .param("grant_type", "password")
        );

        String responseBody = perform.andReturn().getResponse().getContentAsString();
        Jackson2JsonParser parser = new Jackson2JsonParser();

        return parser.parseMap(responseBody).get("access_token").toString();
    }
}