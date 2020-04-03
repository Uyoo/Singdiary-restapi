package com.singdiary.service;

import com.github.pagehelper.PageHelper;
import com.singdiary.common.Description;
import com.singdiary.dao.SongRepository;
import com.singdiary.dto.Song;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SongService {

    @Autowired
    SongRepository songRepository;


    @Description("마이 다이어리 or 그룹 다이어리에 업로드한 곡 삽입")
    public void insertSong(Song newSong) throws Exception {
        this.songRepository.insertSong(newSong);
    }

    @Description("곡 정보 수정")
    public void updateSong(Song newSong) throws Exception{
        this.songRepository.updateSong(newSong);
    }

    @Description("해당 사용자 곡 정보 조회")
    public Song queryUserSong(Song song) throws Exception {
        return this.songRepository.queryUserSong(song);
    }

    @Description("해당 사용자 곡 정보 삭제")
    public void deleteUserSong(Song song) throws Exception {
        this.songRepository.deleteUserSong(song);
    }

    /* ====== 최신곡, 차트 ======*/
    @Description("song 테이블에서 장르별 최신곡 리스트 정보 조회 - 장르가 존재하는 경우")
    public List<Song> queryRecentSongListAllGenre(Integer pageNum, Integer pageSize) throws Exception {
        PageHelper.startPage(pageNum, pageSize);
        return this.songRepository.queryRecentSongListAllGenre();
    }

    @Description("song 테이블에서 장르별 최신곡 리스트 정보 조회 - 장르가 존재하는 경우")
    public List<Song> queryRecentSongList(String genre, Integer pageNum, Integer pageSize) throws Exception {
        PageHelper.startPage(pageNum, pageSize);
        return this.songRepository.queryRecentSongList(genre);
    }
}
