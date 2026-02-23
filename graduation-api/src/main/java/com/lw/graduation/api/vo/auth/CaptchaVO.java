package com.lw.graduation.api.vo.auth;

import lombok.Data;

/**
 * 验证码数据传输对象
 * 用于向前端返回验证码图片的base64编码和唯一标识
 *
 * @author lw
 */
@Data
public class CaptchaVO {

    /**
     * 验证码图片的base64编码，格式为data:image/png;base64,xxx
     */
    private String captchaImg;

    /**
     * 验证码唯一标识，用于后续验证
     */
    private String captchaId;

    /**
     * 构造函数
     */
    public CaptchaVO() {
    }
}