package com.singdiary.dto;

import lombok.*;

import javax.validation.constraints.NotEmpty;

@Data @AllArgsConstructor @NoArgsConstructor
@Getter @Setter
@Builder
public class Group {

    private Integer id;
    @NotEmpty
    private String name;            //그룹명
    private String manager;         //그룹장
    private String profileImage;
    private String backgroundImage;
    private Integer managerId;      //그룹장 id

    private Integer memberCount;    //멤버 수
    private boolean joined;         //가입 되어있는지 여부

    public void init_images() {
        if(profileImage == null || profileImage.equals("")){
            this.profileImage = "default image";
        }
        if(backgroundImage == null || backgroundImage.equals("")){
            this.backgroundImage = "default image";
        }
    }
}
