package com.media.form;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class UploadBase64Form {
    @NotBlank(message = "type is required")
    @ApiModelProperty(name = "type", required = true)
    private String type ;
    @NotBlank(message = "base64 is required")
    @ApiModelProperty(name = "base64", required = true)
    private String base64;
    @NotBlank(message = "ext is required")
    @ApiModelProperty(name = "ext", required = true)
    private String ext;
}
