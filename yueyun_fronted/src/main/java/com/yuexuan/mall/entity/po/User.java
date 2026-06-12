package com.yuexuan.mall.entity.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.time.LocalDate;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 用户表
 * </p>
 *
 * @author huge
 * @since 2026-05-31
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("user")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键，自增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 登录名，唯一
     */
    private String username;

    /**
     * 加密后密码（BCrypt）
     */
    private String password;

    /**
     * 用户昵称/显示名
     */
    private String nickname;

    /**
     * 头像 URL
     */
    private String avatar;

    /**
     * 手机号，可用于登录或短信通知
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 性别：0-未知，1-男，2-女
     */
    private Integer gender;

    /**
     * 生日
     */
    private LocalDate birthday;

    /**
     * 账户状态：1-正常，0-禁用
     */
    private Integer status;

    /**
     * 最近一次登录时间
     */
    private LocalDateTime lastLoginTime;

    /**
     * 逻辑删除：0-未删除，1-已删除
     */
    private Integer isDeleted;

    /**
     * 创建人ID（注册时为0-系统，管理员代建时为 admin.id）
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
