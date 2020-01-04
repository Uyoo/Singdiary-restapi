package com.singdiary.controller;

import com.singdiary.common.AppProperties;
import com.singdiary.common.BaseControllerTest;
import com.singdiary.common.TestDescription;
import com.singdiary.dto.Account;
import com.singdiary.dto.Group;
import com.singdiary.dto.Mydiary;
import com.singdiary.dto.Mydiary_Update;
import com.singdiary.service.AccountService;
import com.singdiary.service.MydiaryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.common.util.Jackson2JsonParser;
import org.springframework.test.web.servlet.ResultActions;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MydiaryControllerTest extends BaseControllerTest {

    @Autowired
    AppProperties appProperties;

    @Autowired
    AccountService accountService;

    @Autowired
    MydiaryService mydiaryService;

    @Test
    @TestDescription("해당 사용자의 마이 다이어리에 곡 업로드 정상 처리")
    public void addUserMydiarySong() throws Exception{
        Mydiary mydiary = Mydiary.builder()
                    .songTitle("song4")
                    .genre("pop")
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

    @Test
    @TestDescription("해당 사용자의 마이 다이어리 곡들 리스트 조회 정상 처리")
    public void queryUserMydiary() throws Exception {
        Account user = this.accountService.findByUsername(appProperties.getUserUsername());

        this.mockMvc.perform(get("/users/{userId}/mydiary", user.getId())
                .header(HttpHeaders.AUTHORIZATION, getBearerToken())
                .param("pageNum", "1")
                .param("pageSize", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
        )
                .andDo(print())
                .andExpect(status().isOk())
        ;
    }

    @Test
    @TestDescription("해당 사용자가 업로드한 마이 다이어리의 곡 하나 정보 조회 정상 처리")
    public void queryUserMydiarySong() throws Exception {
        Account user = this.accountService.findByUsername(appProperties.getUserUsername());
        Mydiary mydiary = Mydiary.builder()
                        .id(1)
                        .build();

        this.mockMvc.perform(get("/users/{userId}/mydiary/{mydiaryId}", user.getId(), mydiary.getId())
                .header(HttpHeaders.AUTHORIZATION, getBearerToken())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
        )
                .andDo(print())
                .andExpect(status().isOk())
        ;
    }

    @Test
    @TestDescription("mydiaryId가 해당 사용자의 곡이 아닌 경우")
    public void queryUserMydiarySong_NotUserSong() throws Exception {
        Account user = this.accountService.findByUsername(appProperties.getUserUsername());
        Mydiary mydiary = Mydiary.builder()
                .id(123122)
                .build();

        this.mockMvc.perform(get("/users/{userId}/mydiary/{mydiaryId}", user.getId(), mydiary.getId())
                .header(HttpHeaders.AUTHORIZATION, getBearerToken())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
        )
                .andDo(print())
                .andExpect(status().isBadRequest())
        ;
    }


    @Test
    @TestDescription("해당 사용자의 마이 다이어리 곡 하나 정보 수정 정상 처리")
    public void patchUserMydiarySong() throws Exception {
        Account user = this.accountService.findByUsername(appProperties.getUserUsername());
        Mydiary mydiary = Mydiary.builder()
                .id(1)
                .build();

        Mydiary userMydiarySong = this.mydiaryService.findUserMydiarySong(user.getId(), mydiary.getId());
        Mydiary_Update mydiaryUpdate = new Mydiary_Update();

        //곡 제목 수정, private 유지
        mydiaryUpdate.setSongTitle("song1_update");
        mydiaryUpdate.setGenre(userMydiarySong.getGenre());
        mydiaryUpdate.setOpen(userMydiarySong.isOpen());

        //곡 제목 유지, public -> private 변경 or private -> public
        /*mydiaryUpdate.setSongTitle(userMydiarySong.getSongTitle());
        mydiaryUpdate.setGenre(userMydiarySong.getGenre());
        mydiaryUpdate.setOpen(!userMydiarySong.isOpen());*/

        this.mockMvc.perform(patch("/users/{userId}/mydiary/{mydiaryId}", user.getId(), mydiary.getId())
                .header(HttpHeaders.AUTHORIZATION, getBearerToken())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(mydiaryUpdate))
        )
                .andDo(print())
                .andExpect(status().isOk())
        ;
    }

    @Test
    @TestDescription("해당 사용자의 마이 다이어리 곡 하나 정보 수정 입력이 비어있는 경우")
    public void patchUserMydiarySong_EmptyData() throws Exception {
        Account user = this.accountService.findByUsername(appProperties.getUserUsername());
        Mydiary mydiary = Mydiary.builder()
                .id(1)
                .build();

        Mydiary userMydiarySong = this.mydiaryService.findUserMydiarySong(user.getId(), mydiary.getId());
        Mydiary_Update mydiaryUpdate = new Mydiary_Update();

        //곡 제목 누락
        mydiaryUpdate.setOpen(userMydiarySong.isOpen());

        this.mockMvc.perform(patch("/users/{userId}/mydiary/{mydiaryId}", user.getId(), mydiary.getId())
                .header(HttpHeaders.AUTHORIZATION, getBearerToken())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(mydiaryUpdate))
        )
                .andDo(print())
                .andExpect(status().isBadRequest())
        ;
    }

    @Test
    @TestDescription("해당 사용자의 마이 다이어리 곡 하나 정보 삭제 정상 처리")
    public void deleteUserMydiarySong() throws Exception {
        Account user = this.accountService.findByUsername(appProperties.getUserUsername());
        Mydiary mydiary = Mydiary.builder()
                .id(4)
                .build();

        this.mockMvc.perform(delete("/users/{userId}/mydiary/{mydiaryId}", user.getId(), mydiary.getId())
                .header(HttpHeaders.AUTHORIZATION, getBearerToken())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
        )
                .andDo(print())
                .andExpect(status().isNoContent())
        ;
    }


    @Test
    @TestDescription("해당 사용자의 마이 다이어리 곡 하나를 그룹에 공유 (한 그룹 씩 가능)")
    public void shareMydiarySongToGroup() throws Exception {
        Mydiary mydiary = Mydiary.builder()
                .id(3)
                .build();

        Group group = Group.builder()
                .id(3)
                .build();

        this.mockMvc.perform(post("/mydiary/{mydiaryId}/share/groups/{groupId}", mydiary.getId(), group.getId())
                .header(HttpHeaders.AUTHORIZATION, getBearerToken())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
        )
                .andDo(print())
                .andExpect(status().isCreated())
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