package com.singdiary.dao;

import com.singdiary.common.Description;
import com.singdiary.dto.Song;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SongRepository {

    @Description("song 테이블에 업로드한 곡 삽입")
    void insertSong(Song newSong) throws Exception;

    @Description("song 테이블에 곡 정보 수정")
    void updateSong(Song newSong) throws Exception;

    @Description("song 테이블에 해당 사용자 곡 정보 조회")
    Song queryUserSong(Song song) throws Exception;

    @Description("song 테이블에서 해당 사용자 곡 정보 삭제")
    void deleteUserSong(Song song) throws Exception;

    /* ====== 최신곡, 차트 ======*/
    @Description("song 테이블에서 장르별 최신곡 리스트 정보 조회 - 전체")
    List<Song> queryRecentSongListAllGenre() throws Exception;

    @Description("song 테이블에서 장르별 최신곡 리스트 정보 조회 - 장르")
    List<Song> queryRecentSongList(String genre) throws Exception;
}
