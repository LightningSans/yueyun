package com.yuexuan.mall.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.yuexuan.mall.service.IOssFileService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;

/**
 * 阿里云 OSS 文件存储实现
 * AccessKey 通过环境变量 OSS_ACCESS_KEY_ID / OSS_ACCESS_KEY_SECRET 获取
 */
@Slf4j
@Service
public class OssFileServiceImpl implements IOssFileService {

    @Value("${aliyun.oss.endpoint}")
    private String endpoint;

    @Value("${aliyun.oss.access-key-id}")
    private String accessKeyId;

    @Value("${aliyun.oss.access-key-secret}")
    private String accessKeySecret;

    @Value("${aliyun.oss.bucket-name}")
    private String bucketName;

    @Value("${aliyun.oss.file-host}")
    private String fileHost;

    private OSS ossClient;

    @PostConstruct
    public void init() {
        if (accessKeyId == null || accessKeyId.isEmpty() || accessKeyId.startsWith("${")) {
            log.warn("OSS AccessKey 未配置，文件上传功能不可用。请设置环境变量 OSS_ACCESS_KEY_ID / OSS_ACCESS_KEY_SECRET");
            return;
        }
        ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        log.info("OSS 客户端初始化完成，bucket: {}", bucketName);
    }

    @PreDestroy
    public void destroy() {
        if (ossClient != null) {
            ossClient.shutdown();
            log.info("OSS 客户端已关闭");
        }
    }

    @Override
    public String upload(MultipartFile file, String type) {
        if (ossClient == null) {
            throw new RuntimeException("OSS 服务未初始化，请检查环境变量配置");
        }
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }

        // 生成存储路径：images/{type}/2026/06/{uuid}.{ext}
        String originalFilename = file.getOriginalFilename();
        String ext = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            ext = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String objectName = fileHost + "/" + type + "/"
                + DateUtil.format(new Date(), "yyyy/MM") + "/"
                + IdUtil.fastSimpleUUID() + ext;

        try (InputStream inputStream = file.getInputStream()) {
            ossClient.putObject(bucketName, objectName, inputStream);
            // 构建公开访问 URL
            String url = "https://" + bucketName + "." + endpoint.replace("https://", "") + "/" + objectName;
            log.info("OSS 上传成功: {}", url);
            return url;
        } catch (IOException e) {
            log.error("OSS 上传失败", e);
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }

    @Override
    public void delete(String fileUrl) {
        if (ossClient == null) return;
        try {
            // 从 URL 中提取 objectName
            URL url = new URL(fileUrl);
            String path = url.getPath();
            if (path.startsWith("/")) {
                path = path.substring(1);
            }
            // 去掉 bucket 前缀
            String objectName = path.substring(path.indexOf("/") + 1);
            ossClient.deleteObject(bucketName, objectName);
            log.info("OSS 删除成功: {}", objectName);
        } catch (Exception e) {
            log.error("OSS 删除失败: {}", fileUrl, e);
        }
    }
}
