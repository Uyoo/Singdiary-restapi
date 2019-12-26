package com.singdiary.controller;

import com.github.pagehelper.Page;
import com.singdiary.common.Description;
import com.singdiary.dto.*;
import com.singdiary.dto.template_get.Paging;
import com.singdiary.dto.template_get.QueryUserMydiary;
import com.singdiary.linkResources.MydiaryResources;
import com.singdiary.linkResources.MydiaryResources_List;
import com.singdiary.service.AccountService;
import com.singdiary.service.MydiaryService;
import com.singdiary.service.SongService;
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

import java.net.URI;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping(produces = MediaTypes.HAL_JSON_VALUE  + ";charset=UTF-8")
public class MydiaryController {

    @Autowired
    AccountService accountService;

    @Autowired
    MydiaryService mydiaryService;

    @Autowired
    SongService songService;

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
        //Account currUser = accountService.findByUsername(currentUser.getName());
        userValidator.validator_matchUser(currentUser, userId, errors);
        if(errors.hasErrors()) return ResponseEntity.badRequest().body(errors);


        //업로드 시간, 클릭 수, 비공개 초기화
        mydiary.init();

        //사용자의 업로드 곡 파일을 우리 서버 파일에 저장
        //(해당 디바이스 경로를 붙여주고 or 사용자 이름을 붙여준다던지)


        //저장된 파일의 경로를 mydiary의 playResource에 넣어주기

        //db에 삽입 + 방금 업로드한 곡 정보 조회
        mydiary.setUserId(userId);
        mydiaryService.insertUserMydiarySong(mydiary);
        Mydiary uploadedSong = mydiaryService.findUserMydiaryUploadSong(mydiary);

        //링크 정보 삽입
        WebMvcLinkBuilder selfLinkBuilder = linkTo(methodOn(MydiaryController.class).addUserMydiarySong(currentUser, userId, mydiary, errors));
        URI createdURI = selfLinkBuilder.slash(uploadedSong.getId()).toUri();
        MydiaryResources mydiaryResources = new MydiaryResources(uploadedSong);

        //self, get, patch, delete, profile
        mydiaryResources.add(selfLinkBuilder.slash(uploadedSong.getId()).withSelfRel());
        mydiaryResources.add(selfLinkBuilder.withRel("get-user-mydiary-songLists"));
        mydiaryResources.add(selfLinkBuilder.withRel("create-user-mydiary-song"));
        mydiaryResources.add(selfLinkBuilder.slash(uploadedSong.getId()).withRel("get-user-mydiary-song"));
        mydiaryResources.add(selfLinkBuilder.slash(uploadedSong.getId()).withRel("update-user-mydiary-song"));
        mydiaryResources.add(selfLinkBuilder.slash(uploadedSong.getId()).withRel("delete-user-mydiary-song"));

        //song 테이블에 기존 body 데이터를 publicCnt(전환된 횟수)와 함께 저장 (publicCnt = 0)
        Song newSong = modelMapper.map(uploadedSong, Song.class);
        newSong.init_publicCnt();
        songService.insertSong(newSong);

        return ResponseEntity.created(createdURI).body(mydiaryResources);
    }


    @Description("해당 사용자의 마이 다이어리 곡들 리스트 조회")
    @GetMapping(value = "/users/{userId}/mydiary")
    public ResponseEntity queryUserMydiary(@CurrentUser Account currentUser,
                                           @PathVariable Integer userId,
                                           @RequestParam(required = false, defaultValue = "0") Integer pageNum,
                                           @RequestParam(required = false, defaultValue = "0") Integer pageSize) throws Exception {

        //요청을 보낸 사용자가 우리 서비스의 회원인지
        if(currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        //요청을 보낸 사용자와 userId가 일치하는지
        if(!deterMatchUser(currentUser, userId)){
            HashMap<String ,String> map = new HashMap<>();
            map.put("message", "마이 다이어리에 대한 권한이 없는 사용자입니다.");
            return ResponseEntity.badRequest().body(map);
        }

        //해당 사용자의 마이 다이어리 곡 목록 조회 (paging 요청까지)
        List<Mydiary> songLists = mydiaryService.findUserMydiaryAll(currentUser.getId(), pageNum, pageSize);

        //각 곡의 정보와 링크를 관리하는 리스트 배열
        List<MydiaryResources> userMydiaryResourcesLists = new LinkedList<>();
        WebMvcLinkBuilder selfLinkBuilder = linkTo(methodOn(MydiaryController.class).queryUserMydiary(currentUser, userId, pageNum, pageSize));
        WebMvcLinkBuilder selfLinkBuilder_Post = linkTo(methodOn(MydiaryController.class).addUserMydiarySong(currentUser, userId, null, null));

        for(Mydiary list : songLists){
            MydiaryResources resources = new MydiaryResources(list);
            WebMvcLinkBuilder selfLink = selfLinkBuilder.slash(list.getId());

            //self, update, delete
            resources.add(selfLink.withSelfRel());
            resources.add(selfLink.withRel("update-user-mydiary-song"));
            resources.add(selfLink.withRel("delete-user-mydiary-song"));

            userMydiaryResourcesLists.add(resources);
        }

        //각 곡들에 대한 데이터 필드와 링크가 담긴 리스트 배열을 관리하는 객체
        Mydiary_List mydiaryLists = new Mydiary_List();
        mydiaryLists.setItems(userMydiaryResourcesLists);

        //self, get, post
        MydiaryResources_List contents = new MydiaryResources_List(mydiaryLists);
        contents.add(selfLinkBuilder.withSelfRel());
        contents.add(selfLinkBuilder.withRel("get-user-mydiary-songLists"));
        contents.add(selfLinkBuilder_Post.withRel("create-user-mydiary-song"));

        //paging 처리
        Page pageHandle = (Page) songLists;
        Paging paging = new Paging();
        paging.setTotalElements(pageHandle.getTotal());
        paging.setTotalPages(pageHandle.getPages());
        paging.setPageSize(pageHandle.getPageSize());
        paging.setPageNum(pageHandle.getPageNum());

        //contents와 paging을 관리하는 템플릿에 삽입
        QueryUserMydiary template = new QueryUserMydiary();
        template.setContents(contents);
        template.setPages(paging);

        return ResponseEntity.ok().body(template);
    }

    @Description("해당 사용자의 마이 다이어리 곡 하나 정보 조회")
    @GetMapping(value = "/users/{userId}/mydiary/{mydiaryId}")
    public ResponseEntity queryUserMydiarySong(@CurrentUser Account currentUser,
                                               @PathVariable Integer userId,
                                               @PathVariable Integer mydiaryId
                                                ) throws Exception {

        //요청을 보낸 사용자가 우리 서비스의 회원인지
        if(currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        //요청을 보낸 사용자와 userId가 일치하는지
        HashMap<String ,String> map = new HashMap<>();
        if(!deterMatchUser(currentUser, userId)){
            map = new HashMap<>();
            map.put("message", "마이 다이어리에 대한 권한이 없는 사용자입니다.");
            return ResponseEntity.badRequest().body(map);
        }

        //곡 조회
        Mydiary userMydiarySong = mydiaryService.findUserMydiarySong(currentUser.getId(), mydiaryId);

        //mydiaryId가 해당 사용자의 곡이 아닌 경우
        if(userMydiarySong == null) {
            map.put("message", "해당 사용자의 곡이 아닙니다.");
            return ResponseEntity.badRequest().body(map);
        }

        WebMvcLinkBuilder selfLinkBuilder = linkTo(methodOn(MydiaryController.class).queryUserMydiarySong(currentUser, userId, mydiaryId));
        MydiaryResources mydiaryResources = new MydiaryResources(userMydiarySong);

        //self, get, update, delete
        mydiaryResources.add(selfLinkBuilder.withSelfRel());
        mydiaryResources.add(selfLinkBuilder.withRel("get-user-mydiary-song"));
        mydiaryResources.add(selfLinkBuilder.withRel("update-user-mydiary-song"));
        mydiaryResources.add(selfLinkBuilder.withRel("delete-user-mydiary-song"));

        return ResponseEntity.ok().body(mydiaryResources);
    }


    @Description("해당 사용자의 마이 다이어리 곡 하나 수정")
    @PatchMapping(value = "/users/{userId}/mydiary/{mydiaryId}")
    public ResponseEntity patchUserMydiarySong(@CurrentUser Account currentUser,
                                               @PathVariable Integer userId,
                                               @PathVariable Integer mydiaryId,
                                               @RequestBody @Valid Mydiary_Update mydiaryUpdate,
                                               Errors errors) throws Exception {

        //곡 제목, public 전환이 가능
        //body의 데이터 입력이 비어있다면 badrequest
        if(errors.hasErrors()) return ResponseEntity.badRequest().body(errors);

        //요청을 보낸 사용자가 우리 서비스의 회원인지
        if(currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        //요청을 보낸 사용자와 userId가 일치하는지
        userValidator.validator_matchUser(currentUser, userId, errors);
        if(errors.hasErrors()) return ResponseEntity.badRequest().body(errors);


        //mydiaryId가 해당 사용자의 곡이 아닌 경우
        //곡 조회
        Mydiary userMydiarySong = mydiaryService.findUserMydiarySong(currentUser.getId(), mydiaryId);

        //mydiaryId가 해당 사용자의 곡이 아닌 경우
        HashMap<String, String> map = new HashMap<>();
        if(userMydiarySong == null) {
            map.put("message", "해당 사용자의 곡이 아닙니다.");
            return ResponseEntity.badRequest().body(map);
        }

        //mydiary 테이블에 업데이트
        userMydiarySong.setSongTitle(mydiaryUpdate.getSongTitle());
        userMydiarySong.setOpen(mydiaryUpdate.isOpen());
        mydiaryService.updateUserMydiarySong(userMydiarySong);

        //groupdiary 테이블 조회

        //없다면 업데이트 x, 있다면 업데이트


        //song 테이블 조회
        Song userSong = songService.queryUserSong(userMydiarySong);
        userSong.setSongTitle(userMydiarySong.getSongTitle());

        //open = true라면
        if(userMydiarySong.isOpen()){
            //publicCnt+1 한 뒤 업데이트
            Integer publicCnt = userSong.getPublicCnt();
            userSong.setPublicCnt(++publicCnt);
            userSong.setOpen(userMydiarySong.isOpen());
        }

        //open = false라면
        else if(!userMydiarySong.isOpen()){
            userSong.setOpen(userMydiarySong.isOpen());
        }

        //song 테이블에 업데이트
        songService.updateSong(userSong);


        //업데이트된 정보 조회
        userMydiarySong = mydiaryService.findUserMydiarySong(currentUser.getId(), mydiaryId);
        MydiaryResources mydiaryResources = new MydiaryResources(userMydiarySong);

        //self, get, update, delete
        WebMvcLinkBuilder selfLinkBuilder = linkTo(methodOn(MydiaryController.class).patchUserMydiarySong(currentUser, userId, mydiaryId, mydiaryUpdate, errors));
        mydiaryResources.add(selfLinkBuilder.withSelfRel());
        mydiaryResources.add(selfLinkBuilder.withRel("get-user-mydiary-song"));
        mydiaryResources.add(selfLinkBuilder.withRel("update-user-mydiary-song"));
        mydiaryResources.add(selfLinkBuilder.withRel("delete-user-mydiary-song"));

        return ResponseEntity.ok().body(mydiaryResources);
    }


    @Description("해당 사용자의 마이 다이어리 곡 하나 삭제")
    @DeleteMapping(value = "/users/{userId}/mydiary/{mydiaryId}")
    public ResponseEntity deleteUserMydiarySong(@CurrentUser Account currentUser,
                                                @PathVariable Integer userId,
                                                @PathVariable Integer mydiaryId) throws Exception {

        //요청을 보낸 사용자가 우리 서비스의 회원인지
        if(currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        //요청을 보낸 사용자와 userId가 일치하는지
        HashMap<String ,String> map = new HashMap<>();
        if(!deterMatchUser(currentUser, userId)){
            map = new HashMap<>();
            map.put("message", "마이 다이어리에 대한 권한이 없는 사용자입니다.");
            return ResponseEntity.badRequest().body(map);
        }

        //곡 조회
        Mydiary userMydiarySong = mydiaryService.findUserMydiarySong(currentUser.getId(), mydiaryId);

        //mydiaryId가 해당 사용자의 곡이 아닌 경우
        if(userMydiarySong == null) {
            map.put("message", "해당 사용자의 곡이 아닙니다.");
            return ResponseEntity.badRequest().body(map);
        }

        //해당 곡 삭제
        mydiaryService.deleteUserMydiarySong(currentUser.getId(), mydiaryId);


        //그룹 다이어리에도 존재하는지 조회 (mydiaryId, userId)
        //있다면 song 테이블에는 삭제 x
        //없다면 song 테이블에도 삭제


        return ResponseEntity.noContent().build();
    }


    private boolean deterMatchUser(Account currentUser, Integer userId) {
        if(currentUser.getId() == userId) return true;
        return false;
    }

}
