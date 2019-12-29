package com.singdiary.dto;

import com.singdiary.linkResources.GroupResources;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
public class Group_List {

    private List<GroupResources> items;
}
