package com.media.controller;

import com.media.dto.ApiMessageDto;
import com.media.dto.UploadFileDto;
import com.media.form.DeleteListFileForm;
import com.media.form.UploadBase64Form;
import com.media.form.UploadFileForm;
import com.media.service.HttpService;
import com.media.service.MediaApiService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/v1/file")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Slf4j
public class FileController extends ABasicController {
    @Autowired
    MediaApiService mediaApiService;
    @Autowired
    private HttpService httpService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<UploadFileDto> upload(@Valid UploadFileForm uploadFileForm, BindingResult bindingResult) {
        String tenantId = userService.tenantId.get();
        if (StringUtils.isBlank(tenantId)) {
            return mediaApiService.storeFile(uploadFileForm, null);
        }
        return mediaApiService.storeFile(uploadFileForm, userService.tenantId.get());
    }

    @PostMapping(value = "/upload-base64", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<UploadFileDto> uploadForBase64(HttpServletRequest request, @Valid @RequestBody UploadBase64Form uploadBase64Form, BindingResult bindingResult) {
        httpService.validateInternalRequest(request);
        String tenantId = userService.tenantId.get();
        if (StringUtils.isBlank(tenantId)) {
            return mediaApiService.storeFileByBase64(uploadBase64Form, null);
        }
        return mediaApiService.storeFileByBase64(uploadBase64Form, userService.tenantId.get());
    }

    @GetMapping("/download/{folder}/{fileName:.+}")
    @Cacheable("images")
    public ResponseEntity<Resource> downloadFile(@PathVariable String folder, @PathVariable String fileName, HttpServletRequest request) throws FileNotFoundException {
        return getResource(folder, fileName, request, false);
    }

    @GetMapping("/download/{tenantId}/{folder}/{fileName:.+}")
    @Cacheable("images")
    public ResponseEntity<Resource> downloadFileWithTenantPath(@PathVariable(name = "tenantId", required = false) String tenantId, @PathVariable String folder, @PathVariable String fileName, HttpServletRequest request) throws FileNotFoundException {
        folder = tenantId + File.separator + folder;
        return getResource(folder, fileName, request, true);
    }

    private ResponseEntity<Resource> getResource(String folder, String fileName, HttpServletRequest request, boolean tenantLocation) {
        Resource resource = mediaApiService.loadFileAsResource(folder, fileName, tenantLocation);
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
//            log.info("Could not determine file type.");
        }
        if (contentType == null) {
            contentType = "application/octet-stream";
        }
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(7776000, TimeUnit.SECONDS))
                .contentType(MediaType.parseMediaType(contentType))
                //.header(HttpHeaders.EXPIRES, expires)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @PostMapping(value = "/delete-list-file", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<String> deleteListFile(@Valid @RequestBody DeleteListFileForm deleteListFileForm, BindingResult bindingResult) {
        ApiMessageDto<String> apiMessageDto = new ApiMessageDto<>();
        String tenantId = userService.tenantId.get();
        if (StringUtils.isBlank(tenantId)) {
            for (String filePath : deleteListFileForm.getFiles()) {
                mediaApiService.deleteFile(filePath, null);
            }
            apiMessageDto.setMessage("Delete list file success");
            return apiMessageDto;
        }
        for (String filePath : deleteListFileForm.getFiles()) {
            mediaApiService.deleteFile(filePath, userService.tenantId.get());
        }
        apiMessageDto.setMessage("Delete list file success");
        return apiMessageDto;
    }

    @DeleteMapping(value = "/delete-folder/{tenantId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<String> deleteFolder(HttpServletRequest request, @PathVariable("tenantId") String tenantId) {
        httpService.validateInternalRequest(request);
        mediaApiService.deleteFolder(tenantId);
        ApiMessageDto<String> apiMessageDto = new ApiMessageDto<>();
        apiMessageDto.setMessage("Delete folder tenant success");
        return apiMessageDto;
    }
}
