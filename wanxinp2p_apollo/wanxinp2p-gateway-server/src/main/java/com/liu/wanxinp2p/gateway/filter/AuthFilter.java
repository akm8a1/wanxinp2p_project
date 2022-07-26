package com.liu.wanxinp2p.gateway.filter;

import com.alibaba.fastjson.JSON;
import com.liu.wanxinp2p.common.util.EncryptUtil;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 继承ZuulFilter实现网关层面的过滤逻辑
 */
@Component
public class AuthFilter extends ZuulFilter {
    /**
     * 是否开启这个过滤器，返回true开启，返回false不启用
     * @return
     */
    @Override
    public boolean shouldFilter() {
        return true;
    }

    /**
     * 过滤器的类型，应该使用前置过滤器 因为token的验证是需要在网关转发微服务之前就进行的
     * 返回"pre"
     * @return
     */
    @Override
    public String filterType() {
        return "pre"; //前置过滤器，可以在请求被路由之前调用
    }

    /**
     * 过滤器的优先级
     * @return
     */
    @Override
    public int filterOrder() {
        return 0;
    }

    /**
     * 过滤器逻辑
     * @return
     */
    @Override
    public Object run() {
        //1.获取Spring Security OAuth2的认证信息对象
        Authentication authentication = SecurityContextHolder.getContext()
                .getAuthentication();
        if(authentication==null || !(authentication instanceof OAuth2Authentication)){
            return null;// 无token访问网关内资源，直接返回null
        }
        //2.将当前登录的用户以及接入客户端的信息放入Map中
        OAuth2Authentication oauth2Authentication=(OAuth2Authentication)authentication;
        Map<String,String> jsonToken = new HashMap<>
                (oauth2Authentication.getOAuth2Request().getRequestParameters());
//        for (Map.Entry<String, String> stringStringEntry : jsonToken.entrySet()) {
//            System.out.println(stringStringEntry.getKey() + ":" + stringStringEntry.getValue());
//        }
        /*3.将jsonToken写入转发微服务的request中，这样微服务就能通过
            request.getParams("jsonToken")获取到了*/
        //请求的上下文环境，也就是网关向微服务转发的请求
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        request.getParameterMap();// 关键步骤，一定要get一下，下面这行代码才能取到值
        Map<String,List<String>> requestQueryParams = ctx.getRequestQueryParams();
        if (requestQueryParams == null) {
            requestQueryParams = new HashMap<>();
        }
        List<String> arrayList = new ArrayList<>();
        //把Token的Map转化为JSON字符串,然后使用UTF8StringBase64进行加密
        arrayList.add(EncryptUtil.encodeUTF8StringBase64(JSON.toJSONString(jsonToken)));
//        System.out.println(arrayList);
        //存到request中
        requestQueryParams.put("jsonToken", arrayList);
        //放到请求的params中
        ctx.setRequestQueryParams(requestQueryParams);
        return null;
    }
}