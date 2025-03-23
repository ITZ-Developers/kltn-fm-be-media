package com.media.service;

import com.media.constant.MediaConstant;
import com.media.dto.ApiMessageDto;
import com.media.dto.ErrorCode;
import com.media.dto.UploadFileDto;
import com.media.exception.BadRequestException;
import com.media.form.UploadBase64Form;
import com.media.form.UploadFileForm;
import com.media.utils.AESUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Service
@Slf4j
public class MediaApiService {
    protected static final String[] UPLOAD_TYPES = new String[]{"LOGO", "AVATAR", "IMAGE", "VIDEO", "DOCUMENT"};
    protected static final String[] AVATAR_EXTENSION = new String[]{"jpeg", "jpg", "gif", "bmp", "png"};
    @Value("${file.upload-dir}")
    private String ROOT_DIRECTORY;
    @Value("${file.secret-key}")
    private String secretKey;
    private Boolean zipEnabled = true;

    /**
     * return file path
     * - General/
     * Video,Media,..
     * - Tenant/
     * - TenantId
     * Video,Media..
     *
     * @param uploadFileForm
     * @return
     */
    public ApiMessageDto<UploadFileDto> storeFile(UploadFileForm uploadFileForm, String tenantId) {
        // Normalize file name
        ApiMessageDto<UploadFileDto> apiMessageDto = new ApiMessageDto<>();
        try {
            boolean contains = Arrays.stream(UPLOAD_TYPES).anyMatch(uploadFileForm.getType()::equalsIgnoreCase);
            if (!contains) {
                apiMessageDto.setResult(false);
                apiMessageDto.setCode("ERROR-UPLOAD-TYPE-INVALID");
                apiMessageDto.setMessage("Type is required in AVATAR or LOGO or IMAGE or VIDEO or DOCUMENT");
                return apiMessageDto;
            }
            String fileName = StringUtils.cleanPath(uploadFileForm.getFile().getOriginalFilename());
            String ext = FilenameUtils.getExtension(fileName);
            boolean extContains = Arrays.stream(AVATAR_EXTENSION).anyMatch(ext::equalsIgnoreCase);
            if ((Objects.equals(uploadFileForm.getType(), "AVATAR")
                    || Objects.equals(uploadFileForm.getType(), "LOGO")
                    || Objects.equals(uploadFileForm.getType(), "IMAGE"))
                    && !extContains) {
                throw new BadRequestException(ErrorCode.FILE_ERROR_FORMAT_INVALID, "File format is invalid");
            }
            //upload to uploadFolder/TYPE/id
            String finalFile = uploadFileForm.getType() + "_" + RandomStringUtils.randomAlphanumeric(10) + "." + ext;
            String typeFolder = File.separator + uploadFileForm.getType();
            Path fileStorageLocation;
            String tenantFolder = "";
            if (tenantId != null) {
                //upload to uploadFolder/restaurantFolder/TYPE/id
                tenantFolder = File.separator + tenantId;
                fileStorageLocation = Paths.get(ROOT_DIRECTORY + MediaConstant.DIRECTORY_TENANT + tenantFolder + typeFolder).toAbsolutePath().normalize();
            } else {
                fileStorageLocation = Paths.get(ROOT_DIRECTORY + MediaConstant.DIRECTORY_GENERAL + typeFolder).toAbsolutePath().normalize();
            }
            Files.createDirectories(fileStorageLocation);
            Path targetLocation = fileStorageLocation.resolve(finalFile);

            // File encryption
            byte[] fileBytes = uploadFileForm.getFile().getBytes();
            String base64FileContent = Base64.getEncoder().encodeToString(fileBytes);
            String encryptedContent = AESUtils.encrypt(secretKey, base64FileContent, true);
            Files.write(targetLocation, Base64.getDecoder().decode(encryptedContent));

            UploadFileDto uploadFileDto = new UploadFileDto();
            uploadFileDto.setFilePath(tenantFolder + typeFolder + File.separator + finalFile);
            apiMessageDto.setData(uploadFileDto);
            apiMessageDto.setMessage("Upload file success");
        } catch (IOException e) {
//            log.error(e.getMessage(), e);
            apiMessageDto.setResult(false);
            apiMessageDto.setMessage(e.getMessage());
        }
        return apiMessageDto;
    }

    public ApiMessageDto<UploadFileDto> storeFileByBase64(UploadBase64Form uploadBase64Form, String tenantId) {
        ApiMessageDto<UploadFileDto> apiMessageDto = new ApiMessageDto<>();
        try {
            boolean contains = Arrays.stream(UPLOAD_TYPES).anyMatch(uploadBase64Form.getType()::equalsIgnoreCase);
            if (!contains) {
                apiMessageDto.setResult(false);
                apiMessageDto.setCode("ERROR-UPLOAD-TYPE-INVALID");
                apiMessageDto.setMessage("Type is required in AVATAR or LOGO or IMAGE or VIDEO or DOCUMENT");
                return apiMessageDto;
            }

            String ext = uploadBase64Form.getExt();
            String finalFile = uploadBase64Form.getType() + "_"
                    + RandomStringUtils.randomAlphanumeric(10) + "." + ext;

            String typeFolder = File.separator + uploadBase64Form.getType();
            Path fileStorageLocation;
            String tenantFolder = "";

            if (tenantId != null) {
                tenantFolder = File.separator + tenantId;
                fileStorageLocation = Paths.get(ROOT_DIRECTORY + MediaConstant.DIRECTORY_TENANT + tenantFolder + typeFolder)
                        .toAbsolutePath().normalize();
            } else {
                fileStorageLocation = Paths.get(ROOT_DIRECTORY + MediaConstant.DIRECTORY_GENERAL + typeFolder)
                        .toAbsolutePath().normalize();
            }

            Files.createDirectories(fileStorageLocation);
            Path targetLocation = fileStorageLocation.resolve(finalFile);

            String encryptedContent = AESUtils.encrypt(secretKey, uploadBase64Form.getBase64(), true);
            Files.write(targetLocation, Base64.getDecoder().decode(encryptedContent));

            UploadFileDto uploadFileDto = new UploadFileDto();
            uploadFileDto.setFilePath(tenantFolder + typeFolder + File.separator + finalFile);
            apiMessageDto.setData(uploadFileDto);
            apiMessageDto.setMessage("Upload file success");

        } catch (IOException e) {
//            log.error(e.getMessage(), e);
            apiMessageDto.setResult(false);
            apiMessageDto.setMessage(e.getMessage());
        }
        return apiMessageDto;
    }

    public void deleteFile(String filePath, String tenantId) {
        File file;
        if (tenantId != null) {
            file = new File(ROOT_DIRECTORY + MediaConstant.DIRECTORY_TENANT + filePath);
        } else {
            file = new File(ROOT_DIRECTORY + MediaConstant.DIRECTORY_GENERAL + filePath);
        }
        System.out.println("======> file path: " + file.getAbsolutePath());
        if (file.exists()) {
            file.delete();
        }

    }

    public Resource loadFileAsResource(String folder, String fileName, boolean tenantLocation) {
        String directory = ROOT_DIRECTORY + MediaConstant.DIRECTORY_GENERAL;
        if (tenantLocation) {
            directory = ROOT_DIRECTORY + MediaConstant.DIRECTORY_TENANT;
        }
        System.out.println("User.home: " + System.getProperty("spring.config.location"));
        System.out.println("get file: " + folder + "/" + fileName + ", path: " + directory);

        try {
            Path fileStorageLocation = Paths.get(directory + File.separator + folder).toAbsolutePath().normalize();
            Path fP = fileStorageLocation.resolve(fileName).normalize();

            String decryptedContent = AESUtils.decrypt(secretKey, Base64.getEncoder().encodeToString(Files.readAllBytes(fP)), zipEnabled);
            byte[] decryptedBytes = Base64.getDecoder().decode(decryptedContent);

            Path tempFile = Files.createTempFile("Decrypted_", fileName);
            Files.write(tempFile, decryptedBytes);

            Resource resource = new UrlResource(tempFile.toUri());
            if (resource.exists()) {
                return resource;
            }
        } catch (Exception ex) {
            log.error("Error getting file: " + folder + "/" + fileName + ", path: " + directory, ex);
        }
        return null;
    }

    public void deleteFolder(String tenantId) {
        if (org.apache.commons.lang3.StringUtils.isBlank(tenantId)) {
            log.error("Cannot delete null folder");
            return;
        }
        File folder;
        String tenantFolder = File.separator + tenantId;
        folder = new File(ROOT_DIRECTORY + MediaConstant.DIRECTORY_TENANT + tenantFolder);

        if (folder.exists() && folder.isDirectory()) {
            try {
                Files.walk(folder.toPath())
                        .sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
                log.info("Deleted folder: " + folder.getAbsolutePath());
                System.out.println("======> folder path: " + folder.getAbsolutePath());
            } catch (IOException e) {
                log.error("Error deleting folder: " + folder.getAbsolutePath(), e);
            }
        } else {
            log.warn("Folder does not exist or is not a directory: " + folder.getAbsolutePath());
        }
    }
}
