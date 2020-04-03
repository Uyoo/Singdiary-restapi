package com.singdiary.controller;

import com.singdiary.common.AwsCloudProperties;
import com.singdiary.common.Description;
import com.singdiary.dto.Account;
import com.singdiary.dto.AccountDto;
import com.singdiary.dto.CurrentUser;
import com.singdiary.linkResources.UserResources;
import com.singdiary.service.AccountService;
import com.singdiary.service.S3Service;
import com.singdiary.validator.UserValidator;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.net.URI;
import java.util.HashMap;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RestController
@RequestMapping(value = "/users", produces = MediaTypes.HAL_JSON_VALUE  + ";charset=UTF-8")
public class UserController {

    @Autowired
    AccountService accountService;

    @Autowired
    S3Service s3Service;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    AwsCloudProperties awsCloudProperties;

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

        //프로필, 배경 이미지 url로 변환
        currUser.setProfileImage(awsCloudProperties.getUrl() + currUser.getProfileImage());
        currUser.setBackgroundImage(awsCloudProperties.getUrl() + currUser.getBackgroundImage());

        WebMvcLinkBuilder selfLinkBuilder = linkTo(UserController.class).slash(currentUser.getId());
        UserResources userResources = new UserResources(currUser);

        //self, get, update, delete
        userResources.add(selfLinkBuilder.withSelfRel());
        userResources.add(selfLinkBuilder.withRel("get-user"));
        userResources.add(selfLinkBuilder.withRel("update-user"));
        userResources.add(selfLinkBuilder.withRel("delete-user"));

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
        newAccount.init_images();

        // 만약 이미지 파일이 존재한다면
        // 사용자의 프로필, 배경 이미지 파일을 우리 서버 파일에 저장
        // (해당 디바이스 경로를 붙여주고 or 사용자 이름을 붙여준다던지)


        // 저장된 파일의 경로를 newUser의 profileImage에 넣어주기


        // role 부여 (admin or user)
        if(newAccount.getName().equals("admin")){
            newAccount.setRole("ADMIN");
        }
        else {
            newAccount.setRole("USER");
        }

        //사용자 정보 삽입
        accountService.insertUser(newAccount);

        //삽입된 유저 정보
        newAccount = accountService.findByUsername(newAccount.getName());
        AccountDto newUser = this.modelMapper.map(newAccount, AccountDto.class);

        WebMvcLinkBuilder selfLinkBuilder = linkTo(UserController.class).slash(newAccount.getId());
        URI createdURI = selfLinkBuilder.toUri();
        UserResources userResources = new UserResources(newUser);

        //self, get, post, update, delete
        userResources.add(selfLinkBuilder.withSelfRel());
        userResources.add(selfLinkBuilder.withRel("get-user"));
        userResources.add(linkTo(UserController.class).withRel("create-user"));
        userResources.add(selfLinkBuilder.withRel("update-user"));
        userResources.add(selfLinkBuilder.withRel("delete-user"));

        return ResponseEntity.created(createdURI).body(userResources);
    }

    @Description("회원 정보 수정 - 아이디, 비밀번호, 프로필, 배경 이미지")
    @PatchMapping
    public ResponseEntity updateUserInfo(@CurrentUser Account currentUser,
                                         @RequestParam(value = "name", required = false) String username,
                                         @RequestParam(value = "password", required = false) String password,
                                         @RequestParam(value = "profileImage", required = false) MultipartFile file_profileImage,
                                         @RequestParam(value = "backgroundImage", required = false) MultipartFile file_backgroundImage) throws Exception {

        if(currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        Account userInfo = accountService.findByUsername(currentUser.getName());

        //name, password, profile, backgroundImage 중 수정할 요소가 있는지 확인
        if(username != null && !username.equals("")
                && !username.equals(userInfo.getName())) {

            userInfo.setName(username);
        }

        if(password != null && !password.equals("")
                && !passwordEncoder.encode(password).equals(userInfo.getPassword())){

            userInfo.setPassword(passwordEncoder.encode(password));
        }

        if(file_profileImage != null && !file_profileImage.equals("")
                && !file_profileImage.getOriginalFilename().equals(userInfo.getProfileImage())) {

            //HashMap<String, String> datas = s3Service.upload(userInfo.getProfileImage(), file_profileImage);
            //userInfo.setProfileImage(datas.get("fileName"));
            //profileImgUrl = datas.get("imgUrl");

            String profileImage = s3Service.upload(userInfo.getProfileImage(), file_profileImage);
            userInfo.setProfileImage(profileImage);
        }

        if(file_backgroundImage != null && !file_backgroundImage.equals("")
                && !file_backgroundImage.getOriginalFilename().equals(userInfo.getBackgroundImage())){

//            HashMap<String, String> datas = s3Service.upload(userInfo.getBackgroundImage(), file_backgroundImage);
//            userInfo.setBackgroundImage(datas.get("fileName"));
//            backgroundImgUrl = datas.get("imgUrl");

            String backgroundImg = s3Service.upload(userInfo.getBackgroundImage(), file_backgroundImage);
            userInfo.setBackgroundImage(backgroundImg);
        }

        //db에 path 저장 (patch)
        accountService.updateUserInfo(userInfo);

        //응답은 url 형태로
        userInfo.setProfileImage(awsCloudProperties.getUrl() + userInfo.getProfileImage());
        userInfo.setBackgroundImage(awsCloudProperties.getUrl() + userInfo.getBackgroundImage());

        AccountDto modifiedUserInfo = this.modelMapper.map(userInfo, AccountDto.class);

        return ResponseEntity.ok(modifiedUserInfo);
    }

}
