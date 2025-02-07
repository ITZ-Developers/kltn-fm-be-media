package com.media.form;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@ApiModel
public class UploadFileForm {
    /**
     * Kieu upload la logo hay avatar.
     */
    @NotEmpty(message = "type is required")
    @ApiModelProperty(name = "type", required = true)
    private String type ;
    @NotNull(message = "file is required")
    @ApiModelProperty(name = "file", required = true)
    private MultipartFile file;
}
