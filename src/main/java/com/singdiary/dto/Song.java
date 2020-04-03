package com.singdiary.dto;

import lombok.*;

@Builder
@AllArgsConstructor @NoArgsConstructor
@Getter @Setter
@Data
public class Song {

    private Integer id;
    private String userName;
    private Integer userId;
    private String profileImage;

    private String songTitle;
    private String playResource;
    private String genre;
    private Integer clickedNum;
    private boolean open;           //true: public, false: private
    private String uploadDate;
    private String uploadTime;
    private String publicDate;      //public 전환된 최초 날짜
    private Integer publicCnt;      //public 전환 횟수

    public void init_publicCnt() {
        this.publicCnt = 0;
    }
}
