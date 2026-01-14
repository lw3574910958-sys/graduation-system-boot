package com.lw.graduation.auth.util;

import cn.hutool.core.util.IdUtil;
import com.google.code.kaptcha.Producer;
import com.lw.graduation.api.vo.auth.CaptchaVO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
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
     * 生成验证码并直接写入HTTP响应（保持原有方法用于兼容）
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
     * @param captchaKey 验证码键
     * @param captchaCode 验证码
     * @return 验证结果
     */
    public boolean validate(String captchaKey, String captchaCode) {
        String storedCode = redisTemplate.opsForValue().get(captchaKey);
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

        String captchaKey = "captcha:" + IdUtil.simpleUUID();
        redisTemplate.opsForValue().set(captchaKey, text, 5, TimeUnit.MINUTES);

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
