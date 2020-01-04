package com.singdiary.dto;

import lombok.*;

import javax.validation.constraints.NotEmpty;
import java.text.SimpleDateFormat;
import java.util.Date;

@AllArgsConstructor @NoArgsConstructor
@Getter @Setter
@Builder
public class GroupDiary {

    private Integer id;
    private Integer groupId;
    private String groupName;
    @NotEmpty
    private String songTitle;
    @NotEmpty
    private String playResource;
    @NotEmpty
    private String genre;
    private Integer clickedNum;
    private boolean open;           //true: public, false: private
    private String uploadDate;
    private String uploadTime;

    private Integer userId;         //업로드한 유저의 id
    private String userName;        //업로드한 유저의 아이디

    public void init() {
        if(clickedNum == null) this.clickedNum = 0;

        //default는 비공개
        this.open = false;


        //데이터가 입력된 시점 String 타입 저장
        SimpleDateFormat formatter = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss");
        Date now = new Date();

        String current = formatter.format(now);
        String[] tokens = current.split(" ");
        this.uploadDate = tokens[0];
        this.uploadTime = tokens[1];
    }
}
