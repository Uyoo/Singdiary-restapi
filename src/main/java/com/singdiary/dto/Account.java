package com.singdiary.dto;

import lombok.*;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor @NoArgsConstructor
@Builder @Getter @Setter
public class Account {

    private Integer id;

    @NotEmpty
    private String name;

    @NotEmpty
    private String password;

    private String profileImage;        //프로필 이미지
    private String backgroundImage;     //배경 이미지
    private int active;                 //로그인 상태 유무
    private String role;                //admin, user

    public void init() {
        if(profileImage == null || profileImage.equals("")){
            this.profileImage = "default image";
        }
        if(backgroundImage == null || backgroundImage.equals("")){
            this.backgroundImage = "default image";
        }

        this.active = 0;    //로그인 성공할 때 active=true
    }
}
