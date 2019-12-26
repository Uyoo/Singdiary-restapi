package com.singdiary.dto;

import lombok.*;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@AllArgsConstructor @NoArgsConstructor
@Getter @Setter
@Builder
public class Mydiary_Update {

    @NotEmpty
    private String songTitle;

    @NotNull
    private boolean open;   //true: public, false: private

}
