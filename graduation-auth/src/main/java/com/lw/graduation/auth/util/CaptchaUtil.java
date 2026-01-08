package com.lw.graduation.auth.util;

import cn.hutool.core.util.IdUtil;
import com.google.code.kaptcha.Producer;
import com.lw.graduation.common.enums.ResponseCode;
import com.lw.graduation.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

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
     * Redis 操作工具类
     */
    private final StringRedisTemplate redisTemplate;

    /**
     * 生成验证码
     *
     * @param response 响应
     * @return 验证码 key
     */
    public String generateCaptcha(HttpServletResponse response) throws IOException {
        String text = kaptchaProducer.createText();
        BufferedImage image = kaptchaProducer.createImage(text);

        String captchaKey = "captcha:" + IdUtil.simpleUUID();
        redisTemplate.opsForValue().set(captchaKey, text, 5, TimeUnit.MINUTES);

        response.setHeader("Content-Type", "image/png");
        ServletOutputStream out = response.getOutputStream();
        ImageIO.write(image, "png", out);
        out.close();

        // 可通过响应头返回 key，前端下次请求携带
        response.setHeader("Captcha-Key", captchaKey);
        return captchaKey;
    }

    /**
     * 验证验证码
     *
     * @param captchaKey  验证码 key
     * @param userInput 用户输入的验证码
     * @return 是否验证通过
     */
    public boolean validate(String captchaKey, String userInput) {
        String correct = redisTemplate.opsForValue().get(captchaKey);
        if (correct == null) {
            // 为避免信息泄露，使用通用错误信息
            throw new BusinessException(ResponseCode.CAPTCHA_ERROR);
        }
        
        // 验证成功后立即删除验证码，防止重放攻击
        boolean isValid = correct.equalsIgnoreCase(userInput);
        if (isValid) {
            redisTemplate.delete(captchaKey); // 一次性使用
        }
        
        return isValid;
    }
}
