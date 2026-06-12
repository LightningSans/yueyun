package com.yuexuan.mall.entity.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 管理员表
 * </p>
 *
 * @author huge
 * @since 2026-05-31
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("admin")
public class Admin implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键自增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 登录名，唯一（后台用）
     */
    private String username;

    /**
     * 加密后的密码（BCrypt）
     */
    private String password;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 手机号（可选）
     */
    private String phone;

    /**
     * 邮箱（可选）
     */
    private String email;

    /**
     * 角色：SUPER_ADMIN（超级管理员）、ADMIN（普通管理员）
     */
    private String role;

    /**
     * 状态：1-在职，0-禁用
     */
    private Integer status;

    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginTime;

    /**
     * 创建人ID（初始超级管理员为0-系统，后续为 SUPER_ADMIN 的 id）
     */
    private Long createBy;

    /**
     * 最后操作人ID（关联 admin.id）
     */
    private Long updateBy;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;


}
