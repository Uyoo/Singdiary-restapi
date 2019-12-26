package com.singdiary.dao;

import com.singdiary.common.Description;
import com.singdiary.dto.Mydiary;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MydiaryRepository {

    /*====== /users/{userId}/mydiary ======*/
    @Description("mydiary 테이블에 곡 정보 삽입")
    void insertUserMydiarySong(Mydiary mydiary) throws Exception;

    @Description("mydiary 테이블에서 해당 유저가 방금 업로드한 곡 조회")
    Mydiary findUserMydiaryUploadSong(Mydiary mydiary) throws Exception;

    @Description("해당 사용자가 업로드한 마이 다이어리의 모든 곡 리스트 조회")
    List<Mydiary> findUserMydiaryAll(Integer userId) throws Exception;

    /*====== /users/{userId}/mydiary/{mydiaryId} ======*/
    @Description("해당 사용자의 마이 다이어리 곡 하나 정보 조회")
    Mydiary findUserMydiarySong(Integer userId, Integer mydiaryId) throws Exception;

    @Description("해당 사용자의 마이 다이어리 곡 하나 정보 수정")
    void updateUserMydiarySong(Mydiary mydiary) throws Exception;

    @Description("해당 사용자의 마이 다이어리 곡 하나 정보 삭제")
    void deleteUserMydiarySong(Integer userId, Integer mydiaryId) throws Exception;
}
