package com.aiminglow.gitblog.controller.common;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

/**
 * @ClassName CommonController
 * @Description 通用功能controller
 * @Author aiminglow
 */
@Controller
public class CommonController {

    @Autowired
    private DefaultKaptcha defaultKaptcha;

    /**
     * @Description: 返回验证码图片的请求
     * @Param: [request, response]
     * @return: void
     * @Author: aiminglow
     */
    @GetMapping("/common/get-verify-code")
    public void getVerifyCode(HttpServletRequest request, HttpServletResponse response) throws Exception {
        byte[] imageJpeg = null;
        ByteArrayOutputStream jpegOutputStream = new ByteArrayOutputStream();
        try {
            String verifyCode = defaultKaptcha.createText();
            request.getSession().setAttribute("verifyCode", verifyCode);
            BufferedImage bufferedImage = defaultKaptcha.createImage(verifyCode);
            ImageIO.write(bufferedImage, "jpg", jpegOutputStream);
        } catch (IllegalArgumentException e) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        imageJpeg = jpegOutputStream.toByteArray();
        // why set those header?
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
        response.setContentType("image/jpeg");
        // is there any other way to send image stream, not servletOutputStream ?
        ServletOutputStream servletOutputStream = response.getOutputStream();
        servletOutputStream.write(imageJpeg);
        servletOutputStream.flush();
        servletOutputStream.close();
    }
}
