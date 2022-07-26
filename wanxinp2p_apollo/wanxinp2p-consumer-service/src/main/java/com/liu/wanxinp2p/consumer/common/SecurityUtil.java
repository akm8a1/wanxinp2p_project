package com.liu.wanxinp2p.consumer.common;

import com.liu.wanxinp2p.api.account.model.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Slf4j
public class SecurityUtil {
    /**
     * 获取当前登录用户
     * 从请求中解析出携带的token，其中有当前正在登录的用户的信息
     */
    public static LoginUser getUser() {
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (servletRequestAttributes != null) {
            HttpServletRequest request = servletRequestAttributes.getRequest();
            Object jwt = request.getAttribute("current_user");
            log.info("当前登录用户:"+jwt);
            if (jwt instanceof  LoginUser) {
                return (LoginUser) jwt;
            }
        }
        return new LoginUser();
    }
}
