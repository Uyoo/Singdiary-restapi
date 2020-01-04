package com.singdiary.controller;

import com.github.pagehelper.Page;
import com.singdiary.common.Description;
import com.singdiary.dto.*;
import com.singdiary.dto.template_get.Paging;
import com.singdiary.dto.template_get.QueryUserGroupDiary;
import com.singdiary.linkResources.GroupDiaryResources;
import com.singdiary.linkResources.GroupDiaryResources_List;
import com.singdiary.service.GroupService;
import com.singdiary.service.GroupdiaryService;
import com.singdiary.service.MydiaryService;
import com.singdiary.service.SongService;
import com.singdiary.validator.GroupValidator;
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
public class GroupDiaryController {

    @Autowired
    GroupService groupService;

    @Autowired
    GroupdiaryService groupdiaryService;

    @Autowired
    MydiaryService mydiaryService;

    @Autowired
    SongService songService;

    private final ModelMapper modelMapper;
    private final UserValidator userValidator;
    private final GroupValidator groupValidator;

    public GroupDiaryController(ModelMapper modelMapper, UserValidator userValidator, GroupValidator groupValidator) {
        this.modelMapper = modelMapper;
        this.userValidator = userValidator;
        this.groupValidator = groupValidator;
    }

    @Description("해당 사용자가 속한 그룹의 다이어리에 곡 추가")
    @PostMapping(value = "/groups/{groupId}/groupdiary")
    public ResponseEntity addUserGroupdiarySong(@CurrentUser Account currentUser,
                                                @PathVariable Integer groupId,
                                                @RequestBody @Valid GroupDiary groupDiary,
                                                Errors errors) throws Exception {

        //입력이 비어있다면
        if(errors.hasErrors()) return ResponseEntity.badRequest().body(errors);

        //요청을 보낸 사용자가 우리 서비스의 회원인지
        if(currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        HashMap<String, String> map = new HashMap<>();
        //해당 그룹이 존재하는지
        Group existGroup = groupService.findGroupByGroupId(groupId);
        if(existGroup == null){
            //map.put("message", "해당 그룹이 존재하지 않습니다");
            map.put("message", "존재하지 않는 그룹입니다");
            return ResponseEntity.badRequest().body(map);
        }

        //해당 유저가 해당 그룹(groupId)에 속해 있는지 판단
        Group group = new Group();
        group.setId(groupId);
        GroupDto userGroup = groupService.findUserGroup(group, currentUser.getId());

        if(userGroup == null){
            map.put("message", "해당 사용자는 그룹원이 아닙니다");
            return ResponseEntity.badRequest().body(map);
        }

        //업로드 시간, 클릭 수, 비공개 초기화
        groupDiary.init();

        //사용자의 업로드 곡 파일을 우리 서버 파일에 저장
        //(해당 디바이스 경로를 붙여주고 or 사용자 이름을 붙여준다던지)


        //저장된 파일의 경로를 groupdiary 테이블의 playResource에 넣어주기

        //groupdiary에 삽입
        groupDiary.setGroupId(groupId);
        groupDiary.setUserId(currentUser.getId());
        groupdiaryService.insertUserGroupDiary(groupDiary);

        //방금 업로드한 곡 정보 조회
        List<GroupDiary> uploadedsong = groupdiaryService.findUserGroupdiaryUploadSong(groupDiary);
        GroupDiary uploadSong = uploadedsong.get(0);

                //링크 정보 삽입
        WebMvcLinkBuilder selfLinkBuilder = linkTo(methodOn(GroupDiaryController.class).addUserGroupdiarySong(currentUser, groupId, groupDiary, errors));
        URI createdURI = selfLinkBuilder.slash(uploadSong.getId()).toUri();

        //self, get, post, profile
        GroupDiaryResources groupDiaryResources = new GroupDiaryResources(uploadSong);
        groupDiaryResources.add(selfLinkBuilder.slash(uploadSong.getId()).withSelfRel());
        groupDiaryResources.add(selfLinkBuilder.withRel("get-user-groupdiary-songLists"));
        groupDiaryResources.add(selfLinkBuilder.slash(uploadSong.getId()).withRel("get-user-groupdiary-song"));
        groupDiaryResources.add(selfLinkBuilder.withRel("create-user-groupdiary-song"));

        //song 테이블에 기존 body 데이터를 publicCnt(전환된 횟수)와 함께 저장 (publicCnt = 0)
        Song newSong = modelMapper.map(uploadSong, Song.class);
        newSong.init_publicCnt();
        songService.insertSong(newSong);

        return ResponseEntity.created(createdURI).body(groupDiaryResources);
    }

    @Description("해당 사용자가 속한 그룹의 다이어리의 곡 리스트 조회")
    @GetMapping(value = "/groups/{groupId}/groupdiary")
    public ResponseEntity queryUserGroupdiarySongs(@CurrentUser Account currentUser,
                                                   @PathVariable Integer groupId,
                                                   @RequestParam(required = false, defaultValue = "0") Integer pageNum,
                                                   @RequestParam(required = false, defaultValue = "0") Integer pageSize) throws Exception {

        //요청을 보낸 사용자가 우리 서비스의 회원인지
        if(currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        HashMap<String, String> map = new HashMap<>();
        //해당 그룹이 존재하는지
        Group existGroup = groupService.findGroupByGroupId(groupId);
        if(existGroup == null){
            //map.put("message", "해당 그룹이 존재하지 않습니다");
            map.put("message", "존재하지 않는 그룹입니다");
            return ResponseEntity.badRequest().body(map);
        }

        //해당 유저가 해당 그룹(groupId)에 속해 있는지 판단
        Group group = new Group();
        group.setId(groupId);
        GroupDto userGroup = groupService.findUserGroup(group, currentUser.getId());
        if(userGroup == null){
            map.put("message", "해당 사용자는 그룹원이 아닙니다");
            return ResponseEntity.badRequest().body(map);
        }

        //해당 그룹의 그룹 다이어리 곡 목록 리스트 조회 (paging 요청까지)
        List<GroupDiary> groupdiarySongLists = groupdiaryService.findUserGroupGroupdiarySongs(groupId, pageNum, pageSize);

        //각 곡의 정보와 링크를 관리하는 리스트 배열
        List<GroupDiaryResources> groupDiaryResourcesList = new LinkedList<>();
        WebMvcLinkBuilder selfLinkBuilder_POST = linkTo(methodOn(GroupDiaryController.class).addUserGroupdiarySong(currentUser, groupId, null, null));
        WebMvcLinkBuilder selfLinkBuilder_GET = linkTo(methodOn(GroupDiaryController.class).queryUserGroupdiarySongs(currentUser, groupId, pageNum, pageSize));

        for(GroupDiary list : groupdiarySongLists){
            GroupDiaryResources resources = new GroupDiaryResources(list);
            WebMvcLinkBuilder selfLink = selfLinkBuilder_POST.slash(list.getId());

            //self, get, update(곡 주인만), delete(곡 주인, 그룹장), profile
            resources.add(selfLink.withSelfRel());
            resources.add(selfLink.withRel("get-user-groupdiary-song"));
            if(currentUser.getId() == list.getUserId()){
                resources.add(selfLink.withRel("update-user-groupdiary-song"));
                resources.add(selfLink.withRel("delete-user-groupdiary-song"));
            }
            else if(currentUser.getId() == userGroup.getManagerId()){
                resources.add(selfLink.withRel("delete-user-groupdiary-song"));
            }

            groupDiaryResourcesList.add(resources);
        }

        GroupDiary_List groupDiaryList = new GroupDiary_List();
        groupDiaryList.setItems(groupDiaryResourcesList);

        //self, get, post, profile
        GroupDiaryResources_List contents = new GroupDiaryResources_List(groupDiaryList);
        contents.add(selfLinkBuilder_GET.withSelfRel());
        contents.add(selfLinkBuilder_GET.withRel("get-user-groupdiary-songLists"));
        contents.add(selfLinkBuilder_POST.withRel("create-user-groupdiary-song"));

        //paging 처리
        Page pageHandle = (Page) groupdiarySongLists;
        Paging paging = new Paging();
        paging.setTotalElements(pageHandle.getTotal());
        paging.setTotalPages(pageHandle.getPages());
        paging.setPageSize(pageHandle.getPageSize());
        paging.setPageNum(pageHandle.getPageNum());

        //contents와 paging을 관리하는 템플릿에 삽입
        QueryUserGroupDiary template = new QueryUserGroupDiary();
        template.setContens(contents);
        template.setPages(paging);

        return ResponseEntity.ok().body(template);
    }


    @Description("해당 사용자가 속한 그룹의 다이어리의 곡 하나 조회")
    @GetMapping(value = "/groups/{groupId}/groupdiary/{groupdiaryId}")
    public ResponseEntity queryUserGroupdiarySong(@CurrentUser Account currentUser,
                                                  @PathVariable Integer groupId,
                                                  @PathVariable Integer groupdiaryId) throws Exception {

        //요청을 보낸 사용자가 우리 서비스의 회원인지
        if(currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        HashMap<String, String> map = new HashMap<>();
        //해당 그룹이 존재하는지
        Group existGroup = groupService.findGroupByGroupId(groupId);
        if(existGroup == null){
            //map.put("message", "해당 그룹이 존재하지 않습니다");
            map.put("message", "존재하지 않는 그룹입니다");
            return ResponseEntity.badRequest().body(map);
        }

        //해당 유저가 해당 그룹(groupId)에 속해 있는지 판단
        Group group = new Group();
        group.setId(groupId);
        GroupDto userGroup = groupService.findUserGroup(group, currentUser.getId());
        if(userGroup == null){
            map.put("message", "해당 사용자는 그룹원이 아닙니다");
            return ResponseEntity.badRequest().body(map);
        }

        //해당 그룹의 그룹 다이어리 곡 하나 조회
        GroupDiary groupdiarySong = groupdiaryService.findUserGroupGroupdiarySong(groupId, groupdiaryId);

        //groupdiaryId가 해당 그룹의 곡인지 확인
        if(groupdiarySong == null){
            map.put("message", "해당 그룹의 곡이 아닙니다");
            return ResponseEntity.badRequest().body(map);
        }

        WebMvcLinkBuilder selfLink = linkTo(methodOn(GroupDiaryController.class).queryUserGroupdiarySong(currentUser, groupId, groupdiaryId));
        WebMvcLinkBuilder selfLinkBuilder_POST = linkTo(methodOn(GroupDiaryController.class).addUserGroupdiarySong(currentUser, groupId, null, null));

        //self, get, create, update(곡 주인), delete(곡 주인, 그룹장), profile
        GroupDiaryResources groupDiaryResources = new GroupDiaryResources(groupdiarySong);
        groupDiaryResources.add(selfLink.withSelfRel());
        groupDiaryResources.add(selfLink.withRel("get-user-groupdiary-song"));
        groupDiaryResources.add(selfLinkBuilder_POST.withRel("create-user-groupdiary-song"));
        if(currentUser.getId() == groupdiarySong.getUserId()){
            groupDiaryResources.add(selfLink.withRel("update-user-groupdiary-song"));
            groupDiaryResources.add(selfLink.withRel("delete-user-groupdiary-song"));
        }

        else if(currentUser.getId() == userGroup.getManagerId()){
            groupDiaryResources.add(selfLink.withRel("delete-user-groupdiary-song"));
        }

        return ResponseEntity.ok().body(groupDiaryResources);
    }

    @Description("해당 사용자가 속한 그룹의 다이어리에 곡 하나 업데이트")
    @PatchMapping(value = "groups/{groupId}/groupdiary/{groupdiaryId}")
    public ResponseEntity patchUserGroupGroupdiarySong(@CurrentUser Account currentUser,
                                                       @PathVariable Integer groupId,
                                                       @PathVariable Integer groupdiaryId,
                                                       @RequestBody @Valid GroupDiary_Update groupDiary_req,
                                                       Errors errors) throws Exception {

        //곡 제목, 장르, public 전환이 가능
        //body의 데이터 입력이 비어있다면 badrequest
        if(errors.hasErrors()) return ResponseEntity.badRequest().body(errors);

        //요청을 보낸 사용자가 우리 서비스의 회원인지
        if(currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        HashMap<String, String> map = new HashMap<>();
        //해당 그룹이 존재하는지
        Group existGroup = groupService.findGroupByGroupId(groupId);
        if(existGroup == null){
            //map.put("message", "해당 그룹이 존재하지 않습니다");
            map.put("message", "존재하지 않는 그룹입니다");
            return ResponseEntity.badRequest().body(map);
        }

        //해당 유저가 해당 그룹(groupId)에 속해 있는지 판단
        Group group = new Group();
        group.setId(groupId);
        GroupDto userGroup = groupService.findUserGroup(group, currentUser.getId());
        if(userGroup == null){
            map.put("message", "해당 사용자는 그룹원이 아닙니다");
            return ResponseEntity.badRequest().body(map);
        }

        //groupdiaryId가 해당 그룹의 곡인지 확인
        GroupDiary groupdiarySong = groupdiaryService.findUserGroupGroupdiarySong(groupId, groupdiaryId);
        if(groupdiarySong == null){
            map.put("message", "해당 그룹의 곡이 아닙니다");
            return ResponseEntity.badRequest().body(map);
        }

        //해당 유저가 곡 주인이어야만 가능
        if(groupdiarySong.getUserId() != currentUser.getId()){
            map.put("message", "곡을 수정할 권한이 없습니다");
            return ResponseEntity.badRequest().body(map);
        }

        //다른 그룹에도 공유했다면 같이 수정해줘야함
        List<GroupDiary> userGroupdiarySongs = groupdiaryService.findUserGroupdiaryUploadSong(groupdiarySong);
        for(GroupDiary userGroupdiarySong : userGroupdiarySongs){
            userGroupdiarySong.setSongTitle(groupDiary_req.getSongTitle());
            userGroupdiarySong.setGenre(groupDiary_req.getGenre());
            userGroupdiarySong.setOpen(groupDiary_req.isOpen());

            //groupdiary 테이블에 업데이트
            groupdiaryService.updateUserGroupdiarySong(userGroupdiarySong);
        }

        groupdiarySong.setSongTitle(groupDiary_req.getSongTitle());
        groupdiarySong.setGenre(groupDiary_req.getGenre());
        groupdiarySong.setOpen(groupDiary_req.isOpen());

        //mydiary 테이블에도 해당 곡이 존재하는지 확인
        Mydiary userMydiarySong = modelMapper.map(groupdiarySong, Mydiary.class);
        Mydiary mydiarySong = mydiaryService.findUserMydiaryUploadSong(userMydiarySong);

        //존재한다면 해당 곡 정보도 mydiary 테이블에서 수정
        if(mydiarySong != null){
            mydiarySong.setSongTitle(userMydiarySong.getSongTitle());
            mydiarySong.setGenre(userMydiarySong.getGenre());
            mydiarySong.setOpen(userMydiarySong.isOpen());

            mydiaryService.updateUserMydiarySong(mydiarySong);
        }

        //song 테이블에도 해당 곡 정보 조회
        Song updateSong = modelMapper.map(userMydiarySong, Song.class);
        Song userSong = songService.queryUserSong(updateSong);
        userSong.setSongTitle(userMydiarySong.getSongTitle());
        userSong.setGenre(userMydiarySong.getGenre());

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

        //업데이트된 그룹 다이어리의 곡 정보 조회
        groupdiarySong = groupdiaryService.findUserGroupGroupdiarySong(groupId, groupdiaryId);

        //self, get, post, patch, delete, profile
        WebMvcLinkBuilder selfLinkBuilder = linkTo(methodOn(GroupDiaryController.class).patchUserGroupGroupdiarySong(currentUser, groupId, groupdiaryId, groupDiary_req, errors));
        GroupDiaryResources groupDiaryResources = new GroupDiaryResources(groupdiarySong);
        groupDiaryResources.add(selfLinkBuilder.withSelfRel());
        groupDiaryResources.add(selfLinkBuilder.withRel("get-user-groupdiary-song"));
        groupDiaryResources.add(selfLinkBuilder.withRel("create-user-groupdiary-song"));
        groupDiaryResources.add(selfLinkBuilder.withRel("update-user-groupdiary-song"));
        groupDiaryResources.add(selfLinkBuilder.withRel("delete-user-groupdiary-song"));

        return ResponseEntity.ok().body(groupDiaryResources);
    }

    @Description("해당 사용자가 속한 그룹의 다이어리에 곡 하나 삭제")
    @DeleteMapping(value = "groups/{groupId}/groupdiary/{groupdiaryId}")
    public ResponseEntity deleteUserGroupGroupdiarySong(@CurrentUser Account currentUser,
                                                        @PathVariable Integer groupId,
                                                        @PathVariable Integer groupdiaryId) throws Exception {

        //요청을 보낸 사용자가 우리 서비스의 회원인지
        if(currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        HashMap<String, String> map = new HashMap<>();
        //해당 그룹이 존재하는지
        Group existGroup = groupService.findGroupByGroupId(groupId);
        if(existGroup == null){
            //map.put("message", "해당 그룹이 존재하지 않습니다");
            map.put("message", "존재하지 않는 그룹입니다");
            return ResponseEntity.badRequest().body(map);
        }

        //해당 유저가 해당 그룹(groupId)에 속해 있는지 판단
        Group group = new Group();
        group.setId(groupId);
        GroupDto userGroup = groupService.findUserGroup(group, currentUser.getId());
        if(userGroup == null){
            map.put("message", "해당 사용자는 그룹원이 아닙니다");
            return ResponseEntity.badRequest().body(map);
        }

        //groupdiaryId가 해당 그룹의 곡인지 확인
        GroupDiary groupdiarySong = groupdiaryService.findUserGroupGroupdiarySong(groupId, groupdiaryId);
        if(groupdiarySong == null){
            map.put("message", "해당 그룹의 곡이 아닙니다");
            return ResponseEntity.badRequest().body(map);
        }

        //해당 유저가 곡 주인 또는 그룹장이어야만 가능
        if(groupdiarySong.getUserId() != currentUser.getId() &&
                userGroup.getManagerId() != currentUser.getId()){

            map.put("message", "곡을 삭제할 권한이 없습니다");
            return ResponseEntity.badRequest().body(map);
        }

        //groupdiary 테이블에서 해당 곡 정보 삭제
        groupdiaryService.deleteUserGroupdiarySong(groupdiarySong.getId(), currentUser.getId());

        //다른 그룹의 다이어리에 해당 곡이 존재하는지 판단
        boolean songRemained = false;
        List<GroupDiary> groupdiarySongList = groupdiaryService.findUserGroupdiaryUploadSong(groupdiarySong);
        if(groupdiarySongList.size() > 0) songRemained = true;

        //마이 다이어리에도 해당 곡이 존재하는지 판단
        if(!songRemained){
            Mydiary mydiary = modelMapper.map(groupdiarySong, Mydiary.class);
            Mydiary userMydiarySong = mydiaryService.findUserMydiaryUploadSong(mydiary);

            if(userMydiarySong != null) songRemained = true;
        }

        //모든 곳에 삭제한 곡의 정보가 존재하지 않는다면 song 테이블에서도 삭제
        if(!songRemained){
            Song deleteSong = modelMapper.map(groupdiarySong, Song.class);
            songService.deleteUserSong(deleteSong);
        }

       return ResponseEntity.noContent().build();
    }

    @Description("해당 사용자의 그룹 다이어리 곡 하나를 마이 다이어리에 공유")
    @PostMapping(value = "/groupdiary/{groupdiaryId}/share/mydiary")
    public ResponseEntity shareGroupDiarySongToMydiary(@CurrentUser Account currentUser,
                                                       @PathVariable Integer groupdiaryId) throws Exception {

        //요청을 보낸 사용자가 우리 서비스의 회원인지
        if(currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        //해당 사용자가 속한 그룹들을 찾고
        HashMap<String, String> map = new HashMap<>();
        List<Group> userGroups = groupService.findUserGroups(currentUser.getId(), 0, 0);
        boolean exist = false;
        GroupDiary userGroupdiarySong = null;

        for(Group group : userGroups){
            //해당 곡이 사용자가 속한 그룹의 곡인지 확인
            userGroupdiarySong = groupdiaryService.findUserGroupGroupdiarySong(group.getId(), groupdiaryId);
            if(userGroupdiarySong != null){
                exist = true;
                break;
            }
        }

        if(!exist){
            map.put("message", "그룹 다이어리에 존재하지 않는 곡입니다");
            return ResponseEntity.badRequest().body(map);
        }

        if(userGroupdiarySong.getUserId() != currentUser.getId()){
            map.put("message", "해당 사용자의 곡이 아닙니다");
            return ResponseEntity.badRequest().body(map);
        }

        //마이 다이어리에 이미 같은 곡이 공유되어있는지 판단
        Mydiary mydiary = modelMapper.map(userGroupdiarySong, Mydiary.class);
        Mydiary sharedSong = mydiaryService.findUserMydiaryUploadSong(mydiary);
        if(sharedSong != null){
            map.put("message", "이미 공유된 곡입니다");
            return ResponseEntity.badRequest().body(map);
        }

        //마이 다이어리에 추가
        mydiaryService.insertUserMydiarySong(mydiary);
        sharedSong = mydiaryService.findUserMydiaryUploadSong(mydiary);

        WebMvcLinkBuilder selfLinkBuilder = linkTo(methodOn(MydiaryController.class).addUserMydiarySong(currentUser, currentUser.getId(), null, null));
        URI createdURI = selfLinkBuilder.slash(sharedSong.getId()).toUri();

        return ResponseEntity.created(createdURI).build();
    }

    @Description("해당 사용자의 그룹 다이어리 곡 하나를 다른 그룹의 그룹 다이어리에 공유")
    @PostMapping(value = "/groupdiary/{groupdiaryId}/share/groups/{groupId}")
    public ResponseEntity shareGroupDiarySongToOtherGroupDiary(@CurrentUser Account currentUser,
                                                               @PathVariable Integer groupdiaryId,
                                                               @PathVariable Integer groupId) throws Exception {

        //요청을 보낸 사용자가 우리 서비스의 회원인지
        if(currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        //해당 사용자가 속한 그룹들을 찾고
        HashMap<String, String> map = new HashMap<>();
        List<Group> userGroups = groupService.findUserGroups(currentUser.getId(), 0, 0);
        boolean exist = false;
        GroupDiary userGroupdiarySong = null;

        for(Group group : userGroups){
            //해당 곡이 사용자가 속한 그룹의 곡인지 확인
            userGroupdiarySong = groupdiaryService.findUserGroupGroupdiarySong(group.getId(), groupdiaryId);
            if(userGroupdiarySong != null){
                exist = true;
                break;
            }
        }

        if(!exist){
            map.put("message", "그룹 다이어리에 존재하지 않는 곡입니다");
            return ResponseEntity.badRequest().body(map);
        }

        if(userGroupdiarySong.getUserId() != currentUser.getId()){
            map.put("message", "해당 사용자의 곡이 아닙니다");
            return ResponseEntity.badRequest().body(map);
        }

        //해당 사용자가 groupId의 그룹원인지 확인
        Group group = new Group();
        group.setId(groupId);
        GroupDto userGroup = groupService.findUserGroup(group, currentUser.getId());
        if(userGroup == null){
            map.put("message", "해당 사용자는 그룹원이 아닙니다");
            return ResponseEntity.badRequest().body(map);
        }

        //현재 그룹에 공유하는 경우 방지
        if(userGroupdiarySong.getGroupId() == groupId){
            map.put("message", "이미 공유된 곡입니다");
            return ResponseEntity.badRequest().body(map);
        }

        //그룹 다이어리에 이미 같은 곡이 공유되어있는지 판단
        List<GroupDiary> sharedSongs = groupdiaryService.findUserGroupdiaryUploadSong(userGroupdiarySong);
        for(GroupDiary song : sharedSongs){
            if(song.getGroupId() == groupId){
                map.put("message", "이미 공유된 곡입니다");
                return ResponseEntity.badRequest().body(map);
            }
        }

        //해당 그룹에 공유
        userGroupdiarySong.setGroupId(groupId);
        groupdiaryService.insertUserGroupDiary(userGroupdiarySong);

        sharedSongs = groupdiaryService.findUserGroupdiaryUploadSong(userGroupdiarySong);
        GroupDiary sharedGroupdiary = null;
        for(GroupDiary song : sharedSongs){
            if(song.getGroupId() == groupId){
                sharedGroupdiary = song;
                break;
            }
        }

        WebMvcLinkBuilder selfLinkBuilder = linkTo(methodOn(GroupController.class).addGroup(currentUser, null, null));
        URI createdURI = selfLinkBuilder.slash(groupId).slash("groupdiary").slash(sharedGroupdiary.getId()).toUri();

        return ResponseEntity.created(createdURI).build();
    }


}
