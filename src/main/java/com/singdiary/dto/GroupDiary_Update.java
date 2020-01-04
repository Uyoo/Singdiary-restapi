package com.singdiary.dto;

import lombok.*;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@AllArgsConstructor @NoArgsConstructor
@Getter @Setter
@Builder
public class GroupDiary_Update {

    @NotEmpty
    private String songTitle;
    @NotEmpty
    private String genre;
    @NotNull
    private boolean open;           //true: public, false: private
}
