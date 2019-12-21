package com.singdiary.dto;

import lombok.*;

@Builder @Getter @Setter
@AllArgsConstructor @NoArgsConstructor
public class AccountDto {

    private Integer id;
    private String name;
    private String profileImage;        //프로필 이미지
    private String backgroundImage;     //배경 이미지
    private int active;                 //로그인 상태 유무
    private String role;                //admin, user

}
