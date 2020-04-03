package com.singdiary.dto;

import lombok.*;

@AllArgsConstructor @NoArgsConstructor
@Builder
@Getter @Setter
public class Account_UpdateInfo {

    private String name;
    private String password;
    private String profileImage;        //프로필 이미지
    private String backgroundImage;     //배경 이미지
}
