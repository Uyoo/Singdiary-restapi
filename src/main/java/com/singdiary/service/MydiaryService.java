package com.singdiary.service;

import com.github.pagehelper.PageHelper;
import com.singdiary.common.Description;
import com.singdiary.dao.MydiaryRepository;
import com.singdiary.dto.Mydiary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MydiaryService {

    @Autowired
    MydiaryRepository mydiaryRepository;

    @Description("해당 유저의 마이 다이어리에 곡 정보 삽입")
    public void insertUserMydiarySong(Mydiary mydiary) throws Exception {
        mydiaryRepository.insertUserMydiarySong(mydiary);
    }

    @Description("마이 다이어리에 업로드한 곡 정보 조회")
    public Mydiary findUserMydiaryUploadSong(Mydiary myDiary) throws Exception {
        return mydiaryRepository.findUserMydiaryUploadSong(myDiary);
    }

    @Description("해당 사용자가 마이 다이어리에 업로드한 모든 곡 리스트 조회")
    public List<Mydiary> findUserMydiaryAll(Integer userId, Integer pageNum, Integer pageSize) throws Exception {
        PageHelper.startPage(pageNum, pageSize);
        return mydiaryRepository.findUserMydiaryAll(userId);
    }

    @Description("해당 사용자의 마이 다이어리에 존재하는 곡 하나 정보 조회")
    public Mydiary findUserMydiarySong(Integer userId, Integer mydiaryId) throws Exception {
        return mydiaryRepository.findUserMydiarySong(userId, mydiaryId);
    }

    @Description("해당 사용자의 마이 다이어리에 존재하는 곡 하나 정보 수정")
    public void updateUserMydiarySong(Mydiary mydiary) throws Exception {
        mydiaryRepository.updateUserMydiarySong(mydiary);
    }

    @Description("해당 사용자의 마이 다이어리에 존재하는 곡 하나 정보 삭제")
    public void deleteUserMydiarySong(Integer userId, Integer mydiaryId) throws Exception {
        mydiaryRepository.deleteUserMydiarySong(userId, mydiaryId);
    }
}
