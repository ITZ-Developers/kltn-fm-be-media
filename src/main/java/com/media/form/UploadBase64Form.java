package com.media.form;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class UploadBase64Form {
    /**
     * Kieu upload la logo hay avatar.
     */
    @NotEmpty(message = "type is required")
    @ApiModelProperty(name = "type", required = true)
    private String type ;
    @NotEmpty(message = "base64Image is required")
    @ApiModelProperty(name = "base64Image", required = true)
    private String base64Image;
}
