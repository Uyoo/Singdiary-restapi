package com.singdiary.dto;

import lombok.*;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@AllArgsConstructor @NoArgsConstructor
@Getter @Setter
@Data @Builder
public class GroupDto {

    private Integer id;
    @NotEmpty
    private String name;            //그룹명
    @NotEmpty
    private String manager;         //그룹장
    @NotEmpty
    private String profileImage;
    @NotEmpty
    private String backgroundImage;
    @NotNull
    private Integer managerId;      //그룹장 id

    private Integer userId;         //그룹원 id
    private String username;        //그룹원 아이디
}
