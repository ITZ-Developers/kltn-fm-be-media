package com.media.form;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class DeleteListFileForm {
    @NotNull(message = "files cannot be null!")
    private List<String> files;
}
