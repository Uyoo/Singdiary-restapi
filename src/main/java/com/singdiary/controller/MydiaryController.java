package com.singdiary.controller;

import com.singdiary.common.Description;
import com.singdiary.dto.Account;
import com.singdiary.dto.CurrentUser;
import com.singdiary.dto.Mydiary;
import com.singdiary.service.AccountService;
import com.singdiary.validator.UserValidator;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping(produces = MediaTypes.HAL_JSON_VALUE  + ";charset=UTF-8")
public class MydiaryController {

    @Autowired
    AccountService accountService;

    private final ModelMapper modelMapper;
    private final UserValidator userValidator;


    public MydiaryController(ModelMapper modelMapper, UserValidator userValidator) {
        this.modelMapper = modelMapper;
        this.userValidator = userValidator;
    }


    @Description("해당 사용자의 마이 다이어리에 곡 하나 생성")
    @PostMapping(value = "/users/{userId}/mydiary")
    public ResponseEntity addUserMydiarySong(@CurrentUser Account currentUser,
                                             @PathVariable Integer userId,
                                             @RequestBody @Valid Mydiary mydiary,
                                             Errors errors) throws Exception {

        //body의 데이터 입력이 비어있다면 badrequest
        if(errors.hasErrors()) return ResponseEntity.badRequest().body(errors);

        //요청을 보낸 사용자가 우리 서비스의 회원인지
        if(currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        //요청을 보낸 사용자와 userId가 일치하는지
        Account currUser = this.accountService.findByUsername(currentUser.getName());
        this.userValidator.validator_matchUser(currUser, userId, errors);
        if(errors.hasErrors()) return ResponseEntity.badRequest().body(errors);


        //업로드 시간, 클릭 수, 비공개 초기화
        mydiary.init();

        //사용자의 업로드 곡 파일을 우리 서버 파일에 저장
        //(해당 디바이스 경로를 붙여주고 or 사용자 이름을 붙여준다던지)


        //저장된 파일의 경로를 mydiary의 playResource에 넣어주기

        //db에 삽입


        //링크 정보 삽입


        //song 테이블에 기존 body 데이터를 publicCnt(전환된 횟수)와 함께 저장 (publicCnt = 0)

        return null;
    }


}
