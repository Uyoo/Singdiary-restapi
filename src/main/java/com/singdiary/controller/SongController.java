package com.singdiary.controller;

import com.singdiary.common.AwsCloudProperties;
import com.singdiary.common.Description;
import com.singdiary.dto.Account;
import com.singdiary.dto.CurrentUser;
import com.singdiary.dto.Song;
import com.singdiary.service.SongService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

@RestController
@RequestMapping(value =  "/songs", produces = MediaTypes.HAL_JSON_VALUE  + ";charset=UTF-8")
public class SongController {

    @Autowired
    SongService songService;

    @Autowired
    AwsCloudProperties awsCloudProperties;

    private final ModelMapper modelMapper;

    public SongController(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    @Description("공개된 곡 중 최신곡 리스트 조회")
    @GetMapping(value = "/recentsongs")
    public ResponseEntity queryRecentSongs(@CurrentUser Account currentUser,
                                           @RequestParam(required = false, defaultValue = "all") String genre,
                                           @RequestParam(required = false, defaultValue = "0") Integer pageNum,
                                           @RequestParam(required = false, defaultValue = "0") Integer pageSize) throws Exception {

        //요청을 보낸 사용자가 우리 서비스의 회원인지
        if(currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

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
