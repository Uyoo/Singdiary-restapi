package com.singdiary.dao;

import com.singdiary.common.Description;
import com.singdiary.dto.Group;
import com.singdiary.dto.GroupDto;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupRepository {

    @Description("groups 테이블에 새로운 그룹 정보 추가")
    void insertGroup(Group newGroup) throws Exception;

    @Description("usergroup 테이블에 그룹에 추가된 사용자 정보 추가")
    void insertUserGroup(Group group, Integer userId) throws Exception;

    @Description("groups 테이블에서 그룹의 이름 중복 확인 or 원하는 그룹 정보 조회")
    Group findGroupByName(String groupname) throws Exception;

    @Description("groups 테이블에서 전체 그룹 목록 조회")
    List<Group> findAllGroups() throws Exception;

    @Description("groups과 usergroup 테이블을 조인하여 해당 사용자가 속한 그룹들 목록 조회")
    List<Group> findUserGroups(Integer userId) throws Exception;

    @Description("groupId를 이용해 groups 테이블에 있는 그룹 하나 조회")
    Group findGroupByGroupId(Integer groupId) throws Exception;

    @Description("groups, usergroup, account 테이블을 조인하여 해당 사용자가 속한 그룹 하나 및 그룹원 정보 조회")
    GroupDto findUserGroup(Group group, Integer userId);

    @Description("groups 테이블에 그룹명, 프로필 및 배경 이미지, 그룹장 위임 정보 업데이트")
    void updateUserGroup(Group group) throws Exception;

}
