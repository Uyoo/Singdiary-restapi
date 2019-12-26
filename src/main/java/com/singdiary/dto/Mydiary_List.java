package com.singdiary.dto;

import com.singdiary.linkResources.MydiaryResources;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Mydiary_List {

    private List<MydiaryResources> items;  //해당 사용자의 마이 다이어리 곡 리스트
}
