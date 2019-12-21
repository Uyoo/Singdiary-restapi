package com.singdiary.controller;

import com.singdiary.common.Description;
import com.singdiary.dto.Account;
import com.singdiary.dto.AccountDto;
import com.singdiary.dto.CurrentUser;
import com.singdiary.linkResources.UserResources;
import com.singdiary.service.AccountService;
import com.singdiary.validator.UserValidator;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RestController
@RequestMapping(value = "/users", produces = MediaTypes.HAL_JSON_VALUE  + ";charset=UTF-8")
public class UserController {

    @Autowired
    AccountService accountService;

    private final ModelMapper modelMapper;
    private final UserValidator userValidator;

    public UserController(ModelMapper modelMapper, UserValidator userValidator) {
        this.modelMapper = modelMapper;
        this.userValidator = userValidator;
    }


    @Description("accesstoken을 받고 회원의 정보 조회")
    @GetMapping
    public ResponseEntity findOurServiceUser(@CurrentUser Account currentUser) throws Exception {

        //사용자 정보가 존재하지 않으면
        if(currentUser == null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        AccountDto currUser = this.modelMapper.map(currentUser, AccountDto.class);
        WebMvcLinkBuilder selfLinkBuilder = linkTo(UserController.class);
        UserResources userResources = new UserResources(currUser);

        //self, get, update, delete
        userResources.add(selfLinkBuilder.withSelfRel());
        userResources.add(selfLinkBuilder.slash(currentUser.getId()).withRel("get-user"));
        userResources.add(selfLinkBuilder.slash(currentUser.getId()).withRel("update-user"));
        userResources.add(selfLinkBuilder.slash(currentUser.getId()).withRel("delete-user"));

        return ResponseEntity.ok().body(userResources);
    }

    @Description("우리 서비스의 회원이 아닌 상태에서 닉네임이 중복된 사용자들의 유무")
    @GetMapping(value = "/duplicate/{username}")
    public ResponseEntity findDuplicateUser(@PathVariable String username) throws Exception{

        HashMap<String, String> map = new HashMap<>();

        //중복되지 않는다면 -> 사용 가능 (not found)
        List<Account> duplicateAccounts = accountService.findDuplicateName(username);
        if(duplicateAccounts.size() == 0){
            return ResponseEntity.notFound().build();
        }

        //중복된다면 -> 닉네임 중복 -> 다른 닉네임 입력 요청
        map.put("message", "이미 존재하는 아이디 입니다.");
        return ResponseEntity.ok().body(map);
    }

    @Description("새로운 사용자 정보 삽입")
    @PostMapping
    public ResponseEntity addUser(@RequestBody @Valid Account newAccount,
                                  Errors errors) throws Exception {

        //요청 값이 비어있으면 badrequest
        if(errors.hasErrors()) return ResponseEntity.badRequest().body(errors);

        //닉네임 해결이 안된 채로 바로 회원가입 누른 경우도 고려 -> badRequest
        //동시에 이미 가입된 회원임을 암시
        List<Account> duplicateAccounts = accountService.findDuplicateName(newAccount.getName());
        userValidator.validator_duplicate(duplicateAccounts, errors);
        if(errors.hasErrors()) return ResponseEntity.badRequest().body(errors);


        //만약 이미지 파일이 없다면 기본 이미지 파일 제공
        newAccount.init();

        // 만약 이미지 파일이 존재한다면
        // 사용자의 프로필, 배경 이미지 파일을 우리 서버 파일에 저장
        // (해당 디바이스 경로를 붙여주고 or 사용자 이름을 붙여준다던지)


        // 저장된 파일의 경로를 newUser의 profileImage에 넣어주기


        // role 부여 (admin or user)
        newAccount.setRole("USER");

        //사용자 정보 삽입
        accountService.insertUser(newAccount);


        //삽입된 유저 정보
        newAccount = accountService.findByUsername(newAccount.getName());
        AccountDto newUser = this.modelMapper.map(newAccount, AccountDto.class);

        WebMvcLinkBuilder selfLinkBuilder = linkTo(UserController.class).slash(newAccount.getId());
        UserResources userResources = new UserResources(newUser);

        //self, get, post, update, delete
        userResources.add(selfLinkBuilder.withSelfRel());
        userResources.add(selfLinkBuilder.withRel("get-user"));
        userResources.add(linkTo(UserController.class).withRel("create-user"));
        userResources.add(selfLinkBuilder.withRel("update-user"));
        userResources.add(selfLinkBuilder.withRel("delete-user"));

        return ResponseEntity.ok().body(userResources);
    }

}
