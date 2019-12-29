package com.singdiary.service;

import com.github.pagehelper.PageHelper;
import com.singdiary.common.Description;
import com.singdiary.dao.GroupRepository;
import com.singdiary.dto.Group;
import com.singdiary.dto.GroupDto;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GroupService {

    @Autowired
    GroupRepository groupRepository;

    @Description("새로운 그룹 정보 추가")
    public void insertGroup(Group newGroup) throws Exception {
        this.groupRepository.insertGroup(newGroup);
    }

    @Description("그룹에 새롭게 추가된 사용자의 정보 추가")
    public void insertUserGroup(@Param("group") Group group, @Param("userId") Integer userId) throws Exception {
        this.groupRepository.insertUserGroup(group, userId);
    }

    @Description("해당 그룹과 이름이 중복되는 그룹 조회 or 그룹의 정보 조회")
    public Group findGroupByName(String groupname) throws Exception {
        return this.groupRepository.findGroupByName(groupname);
    }

    @Description("존재하는 모든 그룹 목록 조회")
    public List<Group> findAllGroups(Integer pageNum, Integer pageSize) throws Exception {
        PageHelper.startPage(pageNum, pageSize);
        return this.groupRepository.findAllGroups();
    }

    @Description("해당 사용자가 속한 그룹 목록들 조회")
    public List<Group> findUserGroups(Integer userId, Integer pageNum, Integer pageSize) throws Exception {
        PageHelper.startPage(pageNum, pageSize);
        return this.groupRepository.findUserGroups(userId);
    }

    @Description("해당 그룹이 존재하는지 조회")
    public Group findGroupByGroupId(Integer groupId) throws Exception {
        return this.groupRepository.findGroupByGroupId(groupId);
    }

    @Description("해당 사용자가 속한 그룹 하나 및 그룹원 정보 조회")
    public GroupDto findUserGroup(Group group, Integer userId) throws Exception {
        return this.groupRepository.findUserGroup(group, userId);
    }

    @Description("그룹명, 프로필 및 배경 이미지, 그룹장 위임 정보 업데이트")
    public void updateUserGroup(Group group) throws Exception {
        this.groupRepository.updateUserGroup(group);
    }

}
