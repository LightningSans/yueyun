package com.yuexuan.mall.controller;

import com.yuexuan.mall.common.R;
import com.yuexuan.mall.service.IOssFileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * 文件上传接口（阿里云 OSS）
 */
@Tag(name = "文件上传")
@RestController
@RequestMapping("/api/file")
public class FileController {

    private final IOssFileService ossFileService;

    public FileController(IOssFileService ossFileService) {
        this.ossFileService = ossFileService;
    }

    @Operation(summary = "上传文件到阿里云 OSS")
    @PostMapping("/upload")
    public R<Map<String, Object>> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "type", defaultValue = "common") String type) {
        if (file.isEmpty()) {
            return R.failed("请选择要上传的文件");
        }
        String url = ossFileService.upload(file, type);
        Map<String, Object> data = new HashMap<>();
        data.put("url", url);
        data.put("fileName", file.getOriginalFilename());
        data.put("size", file.getSize());
        return R.success(data);
    }

    @Operation(summary = "删除文件")
    @DeleteMapping("/delete")
    public R<Void> delete(@RequestParam String url) {
        ossFileService.delete(url);
        return R.success("删除成功");
    }
}
