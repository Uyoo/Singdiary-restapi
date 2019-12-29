package com.singdiary.controller;

import com.github.pagehelper.Page;
import com.singdiary.common.Description;
import com.singdiary.dto.*;
import com.singdiary.dto.template_get.Paging;
import com.singdiary.dto.template_get.QueryGroup;
import com.singdiary.linkResources.GroupResources;
import com.singdiary.linkResources.GroupResources_List;
import com.singdiary.service.GroupService;
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
public class GroupController {

    @Autowired
    GroupService groupService;

    private final ModelMapper modelMapper;
    private final UserValidator userValidator;
    private final GroupValidator groupValidator;

    public GroupController(ModelMapper modelMapper, UserValidator userValidator, GroupValidator groupValidator) {
        this.modelMapper = modelMapper;
        this.userValidator = userValidator;
        this.groupValidator = groupValidator;
    }


    @Description("새로운 그룹 생성")
    @PostMapping(value = "/groups")
    public ResponseEntity addGroup(@CurrentUser Account currentUser,
                                   @RequestBody @Valid Group newGroup,
                                   Errors errors) throws Exception {

        //요청 값(그룹 명)이 비어있으면 badrequest
        if(errors.hasErrors()) return ResponseEntity.badRequest().body(errors);

        //요청을 보낸 사용자가 우리 서비스의 회원인지
        if(currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        //그룹 이름 중복 문제가 해결이 안된 채로 바로 누른 경우도 고려 -> badRequest
        Group duplicateGroups = groupService.findGroupByName(newGroup.getName());
        groupValidator.validator_duplicateName(duplicateGroups, errors);
        if(errors.hasErrors()) return ResponseEntity.badRequest().body(errors);

        //만약 이미지 파일이 없다면 기본 이미지 파일 제공
        newGroup.init_images();

        // 만약 이미지 파일이 존재한다면
        // 그룹의 프로필, 배경 이미지 파일을 우리 서버 파일에 저장
        // (해당 디바이스 경로를 붙여주고 or 사용자 이름을 붙여준다던지)


        // 저장된 파일의 경로를 새로운 그룹의 profileImage에 넣어주기


        //해당 그룹의 생성자(매니저) 정보를 주입하고, group 테이블에 그룹 정보 삽입
        newGroup.setManager(currentUser.getName());
        newGroup.setManagerId(currentUser.getId());
        groupService.insertGroup(newGroup);

        //group 테이블에 생성한 그룹 정보 조회
        Group group = groupService.findGroupByName(newGroup.getName());

        //usergroup 테이블에도 삽입
        groupService.insertUserGroup(group, currentUser.getId());

        //링크 삽입
        WebMvcLinkBuilder selfLinkBuilder = linkTo(methodOn(GroupController.class).addGroup(currentUser, newGroup, errors));
        URI createdURI = selfLinkBuilder.slash(group.getId()).toUri();

        //self, get-groupLists, get-user-groupLists, create, update, delete
        GroupResources groupResources = new GroupResources(group);
        groupResources.add(selfLinkBuilder.slash(group.getId()).withSelfRel());
        groupResources.add(selfLinkBuilder.withRel("create-groups"));
        groupResources.add(selfLinkBuilder.slash(group.getId()).withRel("update-group"));
        groupResources.add(selfLinkBuilder.slash(group.getId()).withRel("delete-group"));

        return ResponseEntity.created(createdURI).body(groupResources);
    }

    @Description("그룹명이 중복되는지 유무 조회")
    @GetMapping(value = "/groups/duplicate/{groupname}")
    public ResponseEntity findDuplicateGroups(@CurrentUser Account currentUser,
                                              @PathVariable String groupname) throws Exception {

        //요청을 보낸 사용자가 우리 서비스의 회원인지
        if(currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        HashMap<String, String> map = new HashMap<>();

        //중복되지 않는다면 -> 사용 가능 (not found)
        Group duplicateGroups = groupService.findGroupByName(groupname);
        if(duplicateGroups == null) return ResponseEntity.notFound().build();

        //중복된다면 -> 닉네임 중복 -> 다른 닉네임 입력 요청
        map.put("message", "이미 존재하는 그룹입니다.");
        return ResponseEntity.ok().body(map);
    }

    @Description("해당 사용자가 속한 그룹들 목록 조회 (쿼리 스트링이 없으면 전체 그룹 목록 조회")
    @GetMapping(value = "/groups")
    public ResponseEntity queryUserGroups(@CurrentUser Account currentUser,
                                          @RequestParam(required = false) String username,
                                          @RequestParam(required = false, defaultValue = "0") Integer pageNum,
                                          @RequestParam(required = false, defaultValue = "0") Integer pageSize) throws Exception {

        //요청을 보낸 사용자가 우리 서비스의 회원인지
        if(currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        WebMvcLinkBuilder selfLinkBuilder_GET = linkTo(methodOn(GroupController.class).queryUserGroups(currentUser, username, pageNum, pageSize));
        WebMvcLinkBuilder selfLinkBuilder_POST = linkTo(methodOn(GroupController.class).addGroup(currentUser, null, null));

        //만약 username이 없다면 => 전체 그룹 목록 조회
        if(username == null) {
            List<Group> allGroups = groupService.findAllGroups(pageNum, pageSize);

            List<GroupResources> groupResourcesList = new LinkedList<>();
            for(Group group : allGroups){
                GroupResources resources = new GroupResources(group);

                //self, get, update(매니저만), delete(매니저만), profile
                resources.add(selfLinkBuilder_POST.slash(group.getId()).withSelfRel());
                resources.add(selfLinkBuilder_POST.slash(group.getId()).withRel("get-a-group"));
                if(group.getManagerId() == currentUser.getId()){
                    resources.add(selfLinkBuilder_POST.slash(group.getId()).withRel("update-a-group"));
                    resources.add(selfLinkBuilder_POST.slash(group.getId()).withRel("delete-a-group"));
                }
                groupResourcesList.add(resources);
            }

            Group_List groupList = new Group_List();
            groupList.setItems(groupResourcesList);

            //self, get(모든 그룹), post, profile
            GroupResources_List contents = new GroupResources_List(groupList);
            contents.add(selfLinkBuilder_GET.withSelfRel());
            contents.add(selfLinkBuilder_GET.withRel("get-groupLists"));
            contents.add(selfLinkBuilder_POST.withRel("create-groups"));

            //paging 처리
            Page pageHandle = (Page) allGroups;
            Paging paging = new Paging();
            paging.setTotalElements(pageHandle.getTotal());
            paging.setTotalPages(pageHandle.getPages());
            paging.setPageSize(pageHandle.getPageSize());
            paging.setPageNum(pageHandle.getPageNum());

            //contents와 paging을 관리하는 템플릿에 삽입
            QueryGroup template = new QueryGroup();
            template.setContents(contents);
            template.setPages(paging);

            return ResponseEntity.ok().body(template);
        }

        //있다면 => 해당 사용자가 속한 그룹 목록 조회
        else {
            //요청을 보낸 사용자와 userId(username)가 일치하는지
            if(!currentUser.getName().equals(username)){
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            //groups와 usergroup 테이블 조인, userId를 이용해 해당 사용자가 속한 그룹 목록 조회
            List<Group> userGroups = groupService.findUserGroups(currentUser.getId(), pageNum, pageSize);

            //해당 사용자가 속한 그룹이 없다면
            if(userGroups.size() == 0 || userGroups == null) {
                HashMap<String, String> map = new HashMap<>();
                map.put("message", "속한 그룹이 존재하지 않습니다");
                return ResponseEntity.ok().body(map);
            }

            List<GroupResources> groupResourcesList = new LinkedList<>();
            for(Group group : userGroups){
                GroupResources resources = new GroupResources(group);

                //self, get, update(매니저만), delete(매니저만), profile
                resources.add(selfLinkBuilder_POST.slash(group.getId()).withSelfRel());
                resources.add(selfLinkBuilder_POST.slash(group.getId()).withRel("get-user-group"));
                if(group.getManagerId() == currentUser.getId()){
                    resources.add(selfLinkBuilder_POST.slash(group.getId()).withRel("update-user-group"));
                    resources.add(selfLinkBuilder_POST.slash(group.getId()).withRel("delete-user-group"));
                }

                groupResourcesList.add(resources);
            }
            Group_List groupList = new Group_List();
            groupList.setItems(groupResourcesList);

            //self, get(해당 사용자가 속한 그룹들), post, profile
            GroupResources_List contents = new GroupResources_List(groupList);
            contents.add(selfLinkBuilder_GET.withSelfRel());
            contents.add(selfLinkBuilder_GET.withRel("get-user-groupLists"));
            contents.add(selfLinkBuilder_POST.withRel("create-user-groups"));

            //paging 처리
            Page pageHandle = (Page) userGroups;
            Paging paging = new Paging();
            paging.setTotalElements(pageHandle.getTotal());
            paging.setTotalPages(pageHandle.getPages());
            paging.setPageSize(pageHandle.getPageSize());
            paging.setPageNum(pageHandle.getPageNum());

            //contents와 paging을 관리하는 템플릿에 삽입
            QueryGroup template = new QueryGroup();
            template.setContents(contents);
            template.setPages(paging);

            return ResponseEntity.ok().body(template);
        }
    }


    @Description("그룹 하나의 정보 조회")
    @GetMapping(value = "/groups/{groupId}")
    public ResponseEntity queryUserGroup(@CurrentUser Account currentUser,
                                         @PathVariable Integer groupId) throws Exception {

        //요청을 보낸 사용자가 우리 서비스의 회원인지
        if(currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        //groupId가 존재하는지 여부
        HashMap<String, String> map = new HashMap<>();
        Group existGroup = groupService.findGroupByGroupId(groupId);
        if(existGroup == null){
            map.put("message", "해당 그룹이 존재하지 않습니다");
            return ResponseEntity.badRequest().body(map);
        }

        //해당 사용자가 그룹원인지 판단 (그룹원이 아니라면 '가입하기', 그룹원이라면 정보 제공)
        GroupDto userGroup = groupService.findUserGroup(existGroup, currentUser.getId());
        if(userGroup == null){
            map.put("message", "그룹원이 아닙니다");
            return ResponseEntity.badRequest().body(map);
        }

        //self, update(manager만), delete(manager만), profile
        WebMvcLinkBuilder selfLinkBuilder = linkTo(methodOn(GroupController.class).queryUserGroup(currentUser, groupId));
        GroupResources groupResources = new GroupResources(existGroup);
        groupResources.add(selfLinkBuilder.withSelfRel());
        if(existGroup.getManagerId() == currentUser.getId()){
            groupResources.add(selfLinkBuilder.withRel("update-a-group"));
            groupResources.add(selfLinkBuilder.withRel("delete-a-group"));
        }

        return ResponseEntity.ok().body(groupResources);
    }

    @Description("그룹 하나의 정보 수정")
    @PatchMapping(value = "/groups/{groupId}")
    public ResponseEntity patchUserGroup(@CurrentUser Account currentUser,
                                         @PathVariable Integer groupId,
                                         @RequestBody @Valid GroupDto groupRequest,
                                         Errors errors) throws Exception {

        //입력이 비어있다면
        if(errors.hasErrors()) return ResponseEntity.badRequest().body(errors);

        //요청을 보낸 사용자가 우리 서비스의 회원인지
        if(currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        //groupId가 존재하는지 여부
        HashMap<String, String> map = new HashMap<>();
        Group existGroup = groupService.findGroupByGroupId(groupId);
        if(existGroup == null){
            map.put("message", "해당 그룹이 존재하지 않습니다");
            return ResponseEntity.badRequest().body(map);
        }

        //그룹장(manager)이 아니라면 수정 및 삭제 불가능
        if(existGroup.getManagerId() != currentUser.getId()){
            map.put("message", "해당 그룹에 대한 수정 권한이 없습니다");
            return ResponseEntity.badRequest().body(map);
        }

        //프로필 or 배경 이미지 경로가 수정되었다면
        if(!groupRequest.getProfileImage().equals(existGroup.getProfileImage())){
            //파일 경로를 새롭게 덮어주고(변경)
            //해당 경로로 다시 갱신

            existGroup.setProfileImage("받아온 경로");
        }

        if(!groupRequest.getBackgroundImage().equals(existGroup.getBackgroundImage())){
            //파일 경로를 새롭게 덮어주고(변경)
            //해당 경로로 다시 갱신

            existGroup.setBackgroundImage("받아온 경로");
        }

        //이름이 수정되었다면 변경된 이름 갱신
        Group duplicateGroup = groupService.findGroupByName(groupRequest.getName());
        if(!existGroup.getName().equals(groupRequest.getName()) && duplicateGroup != null) {
            map.put("message", "이미 존재하는 그룹명입니다");
            return ResponseEntity.badRequest().body(map);
        }
        existGroup.setName(groupRequest.getName());


        //manager 변경이 없는 경우
        if(groupRequest.getManagerId() == currentUser.getId()) {
            existGroup.setManager(currentUser.getName());
            existGroup.setManagerId(currentUser.getId());
        }

        //manager 권한을 위임하는 경우
        else {
            //위임할 managerId가 그룹원인지 판단
            GroupDto member = groupService.findUserGroup(existGroup, groupRequest.getManagerId());
            if(member == null) {
                map.put("message", "해당 사용자는 그룹원이 아닙니다");
                return ResponseEntity.badRequest().body(map);
            }

            existGroup.setManager(member.getUsername());
            existGroup.setManagerId(member.getUserId());
        }

        //groups 테이블에 업데이트
        groupService.updateUserGroup(existGroup);

        //업데이트된 그룹의 정보 조회
        Group updatedGroup = groupService.findGroupByGroupId(groupId);

        //self, update(manager만), delete(manager만), profile
        WebMvcLinkBuilder selfLinkBuilder = linkTo(methodOn(GroupController.class).patchUserGroup(currentUser, groupId, groupRequest, errors));
        GroupResources groupResources = new GroupResources(updatedGroup);
        groupResources.add(selfLinkBuilder.withSelfRel());
        if(updatedGroup.getManagerId() == currentUser.getId()){
            groupResources.add(selfLinkBuilder.withRel("update-user-group"));
            groupResources.add(selfLinkBuilder.withRel("delete-user-group"));
        }

        return ResponseEntity.ok().body(groupResources);
    }


    @Description("그룹 하나의 정보 삭제")
    @DeleteMapping(value = "/groups/{groupId}")
    public ResponseEntity deleteUserGroup(@CurrentUser Account currentUser,
                                         @PathVariable Integer groupId) throws Exception {



        return null;
    }
}
