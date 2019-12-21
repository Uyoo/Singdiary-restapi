package com.singdiary.controller;

import com.singdiary.common.AppProperties;
import com.singdiary.common.BaseControllerTest;
import com.singdiary.common.TestDescription;
import com.singdiary.dto.Account;
import com.singdiary.dto.Mydiary;
import com.singdiary.service.AccountService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.common.util.Jackson2JsonParser;
import org.springframework.test.web.servlet.ResultActions;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MydiaryControllerTest extends BaseControllerTest {

    @Autowired
    AppProperties appProperties;

    @Autowired
    AccountService accountService;

    @Test
    @TestDescription("해당 사용자의 마이 다이어리에 곡 업로드 정상 처리")
    public void addUserMydiarySong() throws Exception{
        Mydiary mydiary = Mydiary.builder()
                    .songTitle("song1")
                    .genre("balad")
                    .playResource("../../")
                    .build();

        Account user = this.accountService.findByUsername(appProperties.getUserUsername());

        this.mockMvc.perform(post("/users/{userId}/mydiary", user.getId())
                                .header(HttpHeaders.AUTHORIZATION, getBearerToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaTypes.HAL_JSON)
                                .content(objectMapper.writeValueAsString(mydiary))
                                )
                            .andDo(print())
                            .andExpect(status().isCreated())
        ;
    }

    @Test
    @TestDescription("곡 제목, 장르, 파일 등의 데이터가 비어있는 경우")
    public void addUserMydiarySong_EmptyData() throws Exception {
        Mydiary mydiary = Mydiary.builder()
                .songTitle("song1")
                .build();

        Account user = this.accountService.findByUsername(appProperties.getUserUsername());

        this.mockMvc.perform(post("/users/{userId}/mydiary", user.getId())
                .header(HttpHeaders.AUTHORIZATION, getBearerToken())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(mydiary))
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].field").exists())
                .andExpect(jsonPath("$[0].objectName").exists())
                .andExpect(jsonPath("$[0].defaultMessage").exists())
        ;
    }

    @Test
    @TestDescription("요청을 보낸 사용자가 회원이 아닌 경우")
    public void addUserMydiarySong_NotOurUser() throws Exception {
        Mydiary mydiary = Mydiary.builder()
                .songTitle("song1")
                .genre("balad")
                .playResource("../../")
                .build();

        Account user = this.accountService.findByUsername(appProperties.getUserUsername());

        this.mockMvc.perform(post("/users/{userId}/mydiary", user.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(mydiary))
        )
                .andDo(print())
                .andExpect(status().isUnauthorized())
        ;
    }

    @Test
    @TestDescription("요청을 보낸 사용자와 userId가 일치하지 않는 경우")
    public void addUserMydiarySong_NotMatchUser() throws Exception {
        Mydiary mydiary = Mydiary.builder()
                .songTitle("song1")
                .genre("balad")
                .playResource("../../")
                .build();

        Account anotherUser = Account.builder()
                        .id(1231231)
                        .build();

        this.mockMvc.perform(post("/users/{userId}/mydiary", anotherUser.getId())
                .header(HttpHeaders.AUTHORIZATION, getBearerToken())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(mydiary))
        )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].objectName").exists())
                .andExpect(jsonPath("$[0].defaultMessage").exists())
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