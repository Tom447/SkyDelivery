package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

/**
 * 公共controller
 */
@Slf4j
@Api(tags = "公共接口")
@RestController
@RequestMapping("/admin/common")
public class CommonController {

    @Autowired
    private AliOssUtil aliOssUtil;

    /**
     * 通用文件上传接口
     */
    @ApiOperation("文件上传")
    @PostMapping("/upload") // ⚠️ 必须加上这个注解！否则无法响应 POST 请求
    public Result<String> upload(MultipartFile file) {
        log.info("文件上传：{}", file.getOriginalFilename());

        try {
            // 1. 校验文件是否为空
            if (file == null || file.isEmpty()) {
                return Result.error("未选择文件");
            }

            // 2. 将文件转换为 byte[]
            byte[] bytes = file.getBytes();

            // 3. 生成唯一的 objectName（避免重名）
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            // 使用 UUID 避免重复
            String objectName = UUID.randomUUID().toString() + extension;

            // 4. 调用 OSS 工具类上传
            String url = aliOssUtil.upload(bytes, objectName);

            // 5. 返回成功结果（code=1 表示成功）
            return Result.success(url);

        } catch (IOException e) {
            log.error("文件读取失败", e);
            return Result.error("文件上传失败");
        } catch (Exception e) {
            log.error("OSS上传异常", e);
            return Result.error("文件上传失败");
        }
    }
}