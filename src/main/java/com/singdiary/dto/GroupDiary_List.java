package com.singdiary.dto;

import com.singdiary.linkResources.GroupDiaryResources;
import lombok.*;

import java.util.List;

@AllArgsConstructor @NoArgsConstructor
@Getter @Setter
@Builder
public class GroupDiary_List {

    private List<GroupDiaryResources> items;
}
