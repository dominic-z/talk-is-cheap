package org.talk.is.cheap.project.what.to.eat.controller;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.talk.is.cheap.project.what.to.eat.constants.ErrorCode;
import org.talk.is.cheap.project.what.to.eat.domain.message.CreateBusinessReq;
import org.talk.is.cheap.project.what.to.eat.domain.message.CreateBusinessResp;
import org.talk.is.cheap.project.what.to.eat.domain.message.UploadBusinessAvatarResp;
import org.talk.is.cheap.project.what.to.eat.domain.pojo.Business;
import org.talk.is.cheap.project.what.to.eat.exceptions.VerificationException;
import org.talk.is.cheap.project.what.to.eat.service.BusinessService;
import org.talk.is.cheap.project.what.to.eat.util.VerifyUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@Slf4j
//@RequestMapping(path = "/")
public class BusinessController {

    @Autowired
    private BusinessService businessService;
    @Value("${file.img-path}")
    private String imgPath;

    @RequestMapping(path = "/api/business", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public CreateBusinessResp createBusiness(@RequestBody CreateBusinessReq req) {
        val createBusinessResp = new CreateBusinessResp();

        try {
            val data = req.getData();
            VerifyUtil.notNull(data.getName(), "business is null");
            val business = new Business().withName(data.getName()).withAvatarPath("null");
            val id = businessService.create(business);
            log.info("{}", business);
            val respBody = new CreateBusinessResp.BusinessRespBody();
            respBody.setId(id);
            createBusinessResp.setData(respBody);
        } catch (Exception e) {
            log.error("create business error", e);
            createBusinessResp.setCode(ErrorCode.ILLEGAL_PARAMETER_ERROR);
            createBusinessResp.setMessage(e.getMessage());
        }

        return createBusinessResp;

    }


    @RequestMapping(path = "/api/businessAvatar", method = RequestMethod.POST)
    @ResponseBody
    public UploadBusinessAvatarResp uploadBusinessAvatar(MultipartFile file, @RequestParam("businessId") Long businessId) {
        val resp = new UploadBusinessAvatarResp();
        try {
            VerifyUtil.notNull(businessId, "business id is null");
            val business = businessService.selectByPrimaryKey(businessId);
            VerifyUtil.notNull(business, ErrorCode.DATA_NOT_FOUND_ERROR, "business with id equal to %d doesn't exists".formatted(businessId));

            final Path imgPath = saveAvatarImg(file, businessId);

            business.setAvatarPath(imgPath.toString());
            val updateCount = businessService.updateByPrimaryKey(businessId, business);
            VerifyUtil.gt(updateCount, 0, ErrorCode.DATA_NOT_FOUND_ERROR, "update avatar path failed");
        } catch (VerificationException e) {
            log.error("upload avatar file failed", e);
            resp.setMessage(e.getMessage());
            resp.setCode(e.getErrorCode());
        } catch (Exception e) {
            log.error("upload avatar file failed", e);
            resp.setMessage(e.getMessage());
            resp.setCode(ErrorCode.ERROR);
        }
        return resp;
    }

    private Path saveAvatarImg(MultipartFile file, Long businessId) throws VerificationException, IOException {
        VerifyUtil.notNull(file, "avatar file is null");
        VerifyUtil.notNull(file.getOriginalFilename(), "avatar file is null");
        val split = file.getOriginalFilename().split("\\.");
        VerifyUtil.gt(split.length, 0, ErrorCode.ILLEGAL_FILE_TYPE_ERROR, "file has no suffix");
        val suffix = split[1];
        log.info("{}",imgPath);
        val imgDirPath = Paths.get("%s/%d".formatted(imgPath,businessId));
        imgDirPath.toFile().mkdirs();
        val imgPath = imgDirPath.resolve("%d_%s.%s".formatted(businessId, UUID.randomUUID(), suffix));
        file.transferTo(imgPath);
        return imgPath;
    }

}
