package com.yuexuan.mall.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * OSS 文件存储服务接口
 * 支持文件上传与删除，当前实现为阿里云 OSS
 */
public interface IOssFileService {

    /**
     * 上传文件到 OSS
     * @param file  上传的文件
     * @param type  文件类型（avatar / product / review / common）
     * @return 文件的公开访问 URL
     */
    String upload(MultipartFile file, String type);

    /**
     * 从 OSS 删除文件
     * @param fileUrl 文件的公开访问 URL
     */
    void delete(String fileUrl);
}
