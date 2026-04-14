package com.lw.graduation.auth.util;

import cn.hutool.core.util.IdUtil;
import com.google.code.kaptcha.Producer;
import com.lw.graduation.api.vo.auth.CaptchaVO;
import com.lw.graduation.common.util.CacheHelper;
import com.lw.graduation.common.constant.CacheConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;


/**
 * 验证码工具类
 *
 * @author lw
 */

@Component
@RequiredArgsConstructor
public class CaptchaUtil {

    /**
     * 验证码生成器
     */
    private final Producer kaptchaProducer;
    /**
     * 缓存工具类
     */
    private  final CacheHelper cacheHelper;

    /**
     * 验证验证码
     *
     * @param captchaKey 验证码键
     * @param captchaCode 验证码
     * @return 验证结果
     */
    public boolean validate(String captchaKey, String captchaCode) {
        String storedCode = cacheHelper.getFromCache(captchaKey, String.class);
        if (storedCode == null) {
            return false; // 验证码不存在或已过期
        }
        // 忽略大小写比较
        return storedCode.equalsIgnoreCase(captchaCode);
    }

    // 保留原有方法，避免冲突
    /**
     * 生成验证码并返回CaptchaDTO对象（新方法，用于返回JSON格式）
     *
     * @return CaptchaDTO 包含验证码图片base64编码和唯一标识
     */
    public CaptchaVO generateCaptchaDto() throws IOException {
        String text = kaptchaProducer.createText();
        BufferedImage image = kaptchaProducer.createImage(text);

        String captchaKey = CacheConstants.KeyPrefix.CAPTCHA_PREFIX + IdUtil.simpleUUID();
        cacheHelper.putToCache(captchaKey, text, CacheConstants.ExpireTime.CAPTCHA_EXPIRE);

        // 将图片转换为base64编码
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(image, "png", os);
        String base64Code = Base64.getEncoder().encodeToString(os.toByteArray());

        // 创建CaptchaDTO对象
        CaptchaVO captcha = new CaptchaVO();
        captcha.setCaptchaImg("data:image/png;base64," + base64Code);
        captcha.setCaptchaId(captchaKey);

        return captcha;
    }


}
