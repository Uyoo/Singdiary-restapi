package com.singdiary.service;

import com.singdiary.common.Description;
import com.singdiary.dao.SongRepository;
import com.singdiary.dto.Mydiary;
import com.singdiary.dto.Song;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    public Song queryUserSong(Mydiary song) throws Exception {
        return this.songRepository.queryUserSong(song);
    }
}
