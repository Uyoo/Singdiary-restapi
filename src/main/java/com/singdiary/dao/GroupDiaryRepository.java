package com.singdiary.dao;

import com.singdiary.common.Description;
import com.singdiary.dto.GroupDiary;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupDiaryRepository {

    @Description("groupdiary 테이블에 곡 정보 추가")
    void insertUserGroupDiary(GroupDiary groupDiary) throws Exception;

    @Description("groupdiary, account, groups를 조인한 테이블에서 해당 유저가 방금 업로드한 곡 조회")
    List<GroupDiary> findUserGroupdiaryUploadSong(GroupDiary groupDiary) throws Exception;

    @Description("groupdiary, account, groups를 조인한 테이블에서 해당 그룹의 곡 목록 조회")
    List<GroupDiary> findUserGroupGroupdiarySongs(Integer groupId) throws Exception;

    @Description("groupdiary, account, groups를 조인한 테이블에서 해당 그룹의 곡 하나 조회")
    GroupDiary findUserGroupGroupdiarySong(Integer groupId, Integer groupdiaryId) throws Exception;

    @Description("groupdiary 테이블에서 해당 사용자의 그룹 다이어리 곡 하나 정보 수정")
    void updateUserGroupdiarySong(GroupDiary groupDiary) throws Exception;

    @Description("groupdiary 테이블에서 해당 사용자의 그룹 다이어리 곡 하나 정보 삭제")
    void deleteUserGroupdiarySong(Integer groupdiaryId, Integer userId) throws Exception;
}
