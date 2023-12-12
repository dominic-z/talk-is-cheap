package org.talk.is.cheap.project.what.to.eat.controller;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.talk.is.cheap.project.what.to.eat.constants.ErrorCode;
import org.talk.is.cheap.project.what.to.eat.domain.message.*;
import org.talk.is.cheap.project.what.to.eat.domain.pojo.Business;
import org.talk.is.cheap.project.what.to.eat.domain.query.example.BusinessExample;
import org.talk.is.cheap.project.what.to.eat.domain.bo.BusinessBO;
import org.talk.is.cheap.project.what.to.eat.exceptions.VerificationException;
import org.talk.is.cheap.project.what.to.eat.service.BusinessService;
import org.talk.is.cheap.project.what.to.eat.util.ResultUtil;
import org.talk.is.cheap.project.what.to.eat.util.VerifyUtil;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequestMapping(path = "/api")
@CrossOrigin("http://localhost:3003/")
public class BusinessController {

    @Autowired
    private BusinessService businessService;
    @Value("${file.img-path}")
    private String imgPath;

    @RequestMapping(path = "/business", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public CreateBusinessResp createBusiness(@RequestBody CreateBusinessReq req) {
        val createBusinessResp = new CreateBusinessResp();

        try {
            val data = req.getData();
            VerifyUtil.notNull(data.getName(), "business is null");
            log.info("{} name length {}", data.getName(), data.getName().length());
//            VerifyUtil.notNull(data.getDescription(), "business description is null");
            val business = new Business()
                    .withName(data.getName())
                    .withDescription(data.getDescription())
                    .withAvatarPath("null");
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


    @RequestMapping(path = "/businessAvatar", method = RequestMethod.POST)
    @ResponseBody
    public UploadBusinessAvatarResp uploadBusinessAvatar(MultipartFile file, @RequestParam("businessId") Long businessId) {
        val resp = new UploadBusinessAvatarResp();
        try {
            VerifyUtil.notNull(businessId, "business id is null");
            val businessList = businessService.selectByPrimaryKeys(List.of(businessId));
            VerifyUtil.isTrue(businessList != null && businessList.size() > 0, ErrorCode.DATA_NOT_FOUND_ERROR, "business with id equal to %d doesn't exists".formatted(businessId));
            val business = businessList.get(0);
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

    @RequestMapping(path = "/business", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
    @ResponseBody
    public GetBusinessListResp getBusinessList(@RequestParam("page") int page, @RequestParam("pageSize") int pageSize) {
        GetBusinessListResp resp = new GetBusinessListResp();
        try {
            VerifyUtil.gt(page, 0, "page should be greater than -1");
            VerifyUtil.gt(pageSize, 0, "pageSize should be greater than 0");

            val respBodyData = new GetBusinessListResp.GetBusinessListRespBody();
            val businessExample = new BusinessExample();
            businessExample.createCriteria().andStatusEqualTo(0);
            val total = businessService.countByExample(businessExample);
            respBodyData.setTotal(total);
            respBodyData.setPage(page);
            respBodyData.setPageSize(pageSize);

            businessExample.clear();
            businessExample.createCriteria().andStatusEqualTo(0);
            businessExample.setOffset((page - 1) * pageSize);
            businessExample.setLimit(pageSize);
            val businessList = businessService.selectByExample(businessExample);

            val businessBOList = businessList.stream().map((b) -> {
                val businessVO = new BusinessBO();
                businessVO.setId(b.getId());
                businessVO.setName(b.getName());
                businessVO.setDescription(b.getDescription());
                return businessVO;
            }).toList();

            respBodyData.setBusinessBOs(businessBOList);

            resp.setCode(0);
            resp.setData(respBodyData);
            return ResultUtil.success(resp);
        } catch (VerificationException e) {
            log.error("verificationException: ", e);
            return ResultUtil.fail(resp, ErrorCode.ILLEGAL_PARAMETER_ERROR, "Verification error");
        } catch (Exception e) {
            log.error("Uncaught Error: ", e);
            return ResultUtil.fail(resp, ErrorCode.ERROR, "uncaught error");
        }
    }

    private Path saveAvatarImg(MultipartFile file, Long businessId) throws VerificationException, IOException {
        VerifyUtil.notNull(file, "avatar file is null");
        VerifyUtil.notNull(file.getOriginalFilename(), "avatar file is null");
        val split = file.getOriginalFilename().split("\\.");
        VerifyUtil.gt(split.length, 0, ErrorCode.ILLEGAL_FILE_TYPE_ERROR, "file has no suffix");
        val suffix = split[1];
        log.info("{}", imgPath);
        val imgDirPath = Paths.get("%s/%d".formatted(imgPath, businessId));
        imgDirPath.toFile().mkdirs();
        val imgPath = imgDirPath.resolve("%d_%s.%s".formatted(businessId, UUID.randomUUID(), suffix));
        file.transferTo(imgPath);
        return imgPath;
    }


    @RequestMapping(value = "/business", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public DeleteBusinessResp deleteBusiness(@Param("id") Long id) {
        val resp = new DeleteBusinessResp();
        try {
            VerifyUtil.notNull(id, "id shouldn't be null");
            val businessList = businessService.selectByPrimaryKeys(List.of(id));
            VerifyUtil.notNull(businessList, ErrorCode.DATA_NOT_FOUND_ERROR, "can't find business by %d".formatted(id));
            VerifyUtil.isTrue(businessList.size() == 0, ErrorCode.DATA_NOT_FOUND_ERROR, "can't find business by %d".formatted(id));
            val business = businessList.get(0);
            business.setName("%s_%d".formatted(business.getName(), new Date().getTime()));
            business.setStatus(1);
            businessService.updateByPrimaryKey(id, business);

            return ResultUtil.success(resp);
        } catch (VerificationException e) {
            return ResultUtil.fail(resp, e.getErrorCode(), e.getMessage());
        } catch (Exception e) {
            return ResultUtil.fail(resp, ErrorCode.ERROR, e.getMessage());
        }
    }


    @RequestMapping(value = "/business", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public UpdateBusinessResp updateBusiness(@RequestBody UpdateBusinessReq req) {
        val resp = new UpdateBusinessResp();
        try {
            val businessBOMap = req.getData().getBusinessBOList().stream().collect(Collectors.toMap(BusinessBO::getId, b -> b));
            val modifyBusinessList = businessService
                    .selectByPrimaryKeys(businessBOMap.keySet().stream().toList())
                    .stream()
                    .peek(b -> BeanUtils.copyProperties(businessBOMap.get(b.getId()), b)).toList();

            val updateCount = businessService.updateBusiness(modifyBusinessList);
            VerifyUtil.eq(updateCount, req.getData().getBusinessBOList().size(), "部分更新失败");
            return ResultUtil.success(resp);
        } catch (VerificationException e) {
            return ResultUtil.fail(resp, e.getErrorCode(), e.getMessage());
        } catch (Exception e) {
            return ResultUtil.fail(resp, ErrorCode.ERROR, e.getMessage());
        }
    }

}
