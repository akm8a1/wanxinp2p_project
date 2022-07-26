package com.liu.wanxinp2p.repayment.interceptor;

import com.alibaba.fastjson.JSON;
import com.liu.wanxinp2p.api.account.model.LoginUser;
import com.liu.wanxinp2p.common.util.EncryptUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Token拦截器 在请求到达Controller之前将token信息解析
 */
public class TokenInterceptor implements HandlerInterceptor {
	/**
	 * 前置拦截器
	 * @param request
	 * @param response
	 * @param handler
	 * @return
	 * @throws Exception
	 */
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		String jsonToken = request.getParameter("jsonToken");
		if (StringUtils.isNotBlank(jsonToken)) {
			LoginUser loginUser = JSON.parseObject(EncryptUtil.decodeUTF8StringBase64(jsonToken),
					LoginUser.class);
			request.setAttribute("current_user",loginUser);
		}
		//放行
		return true;
	}
}
