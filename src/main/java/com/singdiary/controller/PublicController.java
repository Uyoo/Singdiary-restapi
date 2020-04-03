package com.singdiary.controller;

import com.singdiary.common.Description;
import com.singdiary.dto.Account;
import com.singdiary.dto.CurrentUser;
import com.singdiary.dto.Song;
import com.singdiary.service.SongService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedList;
import java.util.List;

@RestController
@RequestMapping(value = "/public", produces = MediaTypes.HAL_JSON_VALUE  + ";charset=UTF-8")
public class PublicController {

    @Autowired
    SongService songService;

    private final ModelMapper modelMapper;

    public PublicController(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    @Description("로그인 없이도 조회 가능한 public된 최근곡 리스트 조회")
    @GetMapping(value = "/songs/recentsongs")
    public ResponseEntity queryRecentSong_NotLogined(@RequestParam(required = false, defaultValue = "all") String genre,
                                                     @RequestParam(required = false, defaultValue = "0") Integer pageNum,
                                                     @RequestParam(required = false, defaultValue = "0") Integer pageSize) throws Exception{

        //db 조회
        genre = genre.toLowerCase();
        List<Song> songList;
        if(genre.equals("all")){
            songList = this.songService.queryRecentSongListAllGenre(pageNum, pageSize);
        }
        else {
            songList = this.songService.queryRecentSongList(genre, pageNum, pageSize);
        }

        //404 not found
        if(songList.size() == 0){
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(songList);
    }

}
