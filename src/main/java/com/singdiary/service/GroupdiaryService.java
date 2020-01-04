package com.singdiary.service;

import com.github.pagehelper.PageHelper;
import com.singdiary.common.Description;
import com.singdiary.dao.GroupDiaryRepository;
import com.singdiary.dto.GroupDiary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GroupdiaryService {

    @Autowired
    GroupDiaryRepository groupDiaryRepository;

    @Description("해당 사용자가 그룹 다이어리에 곡을 업로드 하는 경우")
    public void insertUserGroupDiary(GroupDiary groupDiary) throws Exception {
        this.groupDiaryRepository.insertUserGroupDiary(groupDiary);
    }

    @Description("그룹 다이어리에 업로드한 곡 정보 조회")
    public List<GroupDiary> findUserGroupdiaryUploadSong(GroupDiary groupDiary) throws Exception {
        return this.groupDiaryRepository.findUserGroupdiaryUploadSong(groupDiary);
    }

    @Description("해당 유저가 속한 그룹의 곡 목록 조회")
    public List<GroupDiary> findUserGroupGroupdiarySongs(Integer groupId, Integer pageNum, Integer pageSize) throws Exception {
        PageHelper.startPage(pageNum, pageSize);
        return this.groupDiaryRepository.findUserGroupGroupdiarySongs(groupId);
    }

    @Description("해당 유저가 속한 그룹의 곡 목록 조회")
    public GroupDiary findUserGroupGroupdiarySong(Integer groupId, Integer groupdiaryId) throws Exception {
        return this.groupDiaryRepository.findUserGroupGroupdiarySong(groupId, groupdiaryId);
    }

    @Description("해당 사용자의 그룹 다이어리 곡 하나 정보 수정")
    public void updateUserGroupdiarySong(GroupDiary groupDiary) throws Exception {
        this.groupDiaryRepository.updateUserGroupdiarySong(groupDiary);
    }

    @Description("해당 사용자의 그룹 다이어리 곡 하나 정보 삭제")
    public void deleteUserGroupdiarySong(Integer groupdiaryId, Integer userId) throws Exception {
        this.groupDiaryRepository.deleteUserGroupdiarySong(groupdiaryId, userId);
    }
}
