package com.liu.wanxinp2p.gateway.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.web.AuthenticationEntryPoint;

/**
 * 资源服务器权限配置 内部有各个微服务的资源服务权限配置
 */
@Configuration
public class ResouceServerConfig {

    public static final String RESOURCE_ID = "wanxin-resource";

    private AuthenticationEntryPoint point = new RestOAuth2AuthExceptionEntryPoint();

    private RestAccessDeniedHandler handler = new RestAccessDeniedHandler();

    /**
     * 统一认证中心 资源拦截
     */
    @Configuration
    //资源服务器
    @EnableResourceServer
    public class UAAServerConfig extends
            ResourceServerConfigurerAdapter {

        @Autowired
        private TokenStore tokenStore;

        @Override
        public void configure(ResourceServerSecurityConfigurer resources)
                throws Exception {
            resources.tokenStore(tokenStore).resourceId(RESOURCE_ID)
                    .stateless(true);
        }

        @Override
        public void configure(HttpSecurity http) throws Exception {

            http.sessionManagement()
                    .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                    .and()
                    .authorizeRequests()
                    .antMatchers("/uaa/druid/**").denyAll()
                    .antMatchers("/uaa/**").permitAll();
        }

    }




    /**
     * c端用户服务 资源拦截
     */
    @Configuration
    //资源服务器
    @EnableResourceServer
    public class ConsumerServerConfig extends
            ResourceServerConfigurerAdapter {

        @Autowired
        private TokenStore tokenStore;

        @Override
        public void configure(ResourceServerSecurityConfigurer resources)
                throws Exception {
            resources.tokenStore(tokenStore).resourceId(RESOURCE_ID)
                    .stateless(true);

            resources.authenticationEntryPoint(point).accessDeniedHandler(handler);
        }

        /**
         * Consumer统一用户服务拦截
         * @param http
         * @throws Exception
         */
        @Override
        public void configure(HttpSecurity http) throws Exception {

            http.sessionManagement()
                    .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                    .and()
                    .authorizeRequests()
                    .antMatchers("/consumer/l/**").denyAll() //Consumer远程调用服务
                    //对于受保护的C端用户接口(登录之后使用)所使用的拦截策略
                    //接入方需要有Scope:read 以及ROLE_CONSUMER角色，这些信息存放在数据库中
                    .antMatchers("/consumer/my/**").access("#oauth2.hasScope('read') and #oauth2.clientHasRole('ROLE_CONSUMER')")
                    //对于受保护的B端用户接口(登录之后使用)所使用的拦截策略，接入方需要有Scope:read 以及ROLE_ADMIN角色
                    .antMatchers("/consumer/m/**").access("#oauth2.hasScope('read') and #oauth2.clientHasRole('ROLE_ADMIN')")
                    //其他的资源不会被拦截，都可以访问
                    .antMatchers("/consumer/**").permitAll();
        }
    }
}
