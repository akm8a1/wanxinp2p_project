package com.liu.wanxinp2p.uaa.domain;

import com.liu.wanxinp2p.api.account.model.AccountDTO;
import com.liu.wanxinp2p.api.account.model.AccountLoginDTO;
import com.liu.wanxinp2p.common.domain.RestResponse;
import com.liu.wanxinp2p.common.util.StringUtil;
import com.liu.wanxinp2p.uaa.agent.AccountServiceAgent;
import com.liu.wanxinp2p.uaa.common.utils.ApplicationContextHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class IntegrationUserDetailsAuthenticationHandler {

	/**
	 * 认证处理，此方法返回UserDetails对象给SpringSecurity OAuth2框架，框架根据UserDetails中的信息自动生成
	 * JWT令牌，令牌中存储用户的相关信息
	 * @param domain 用户域 ，如b端用户、c端用户等
	 * @param authenticationType  认证类型，如密码认证，短信认证等
	 * @param token 封装前端传过来的用户名和密码等需要认证的信息
	 * @return SpringSecurity对象，存放登陆成功之后的返回信息，比如账号的基本信息、权限、资源等
	 */
	public UnifiedUserDetails authentication(String domain, String authenticationType,
											 UsernamePasswordAuthenticationToken token) {
		//1.从客户端取出数据
		String username = token.getName();
		if (StringUtil.isBlank(username)) {
			//抛出SpringSecurity OAuth 框架提供的异常
			throw new BadCredentialsException("账户为空");
		}
		if (token.getCredentials() == null) {
			throw new BadCredentialsException("密码为空");
		}
		String presentedPassword = token.getCredentials().toString();
		System.out.println("presentedPassword:"+presentedPassword);
		//2.调用统一账号服务，进行账户密码校验
		AccountLoginDTO accountLoginDTO = new AccountLoginDTO();
		accountLoginDTO.setDomain(domain);
		accountLoginDTO.setUsername(username);
		accountLoginDTO.setMobile(username);
		accountLoginDTO.setPassword(presentedPassword);
		//直接从容器中取出来代理对象
		AccountServiceAgent agent = (AccountServiceAgent) ApplicationContextHelper.getBean(AccountServiceAgent.class);
		System.out.println("accountLoginDTO:"+accountLoginDTO);
		RestResponse<AccountDTO> response = agent.login(accountLoginDTO);
		System.out.println(response);
		//3.异常处理
		if (response.getCode() != 0) {
			//如果Code!=0 说明抛出了异常，认证失败了
			throw  new BadCredentialsException("登陆失败");
		}
		//4.登录成功，把用户数据分装到UserDetails对象
		UnifiedUserDetails unifiedUserDetails = new UnifiedUserDetails(username,presentedPassword,AuthorityUtils.createAuthorityList());
		unifiedUserDetails.setMobile(response.getResult().getMobile());
		return unifiedUserDetails;

	}

	private UnifiedUserDetails getUserDetails(String username) {
		Map<String, UnifiedUserDetails> userDetailsMap = new HashMap<>();
		userDetailsMap.put("admin",
				new UnifiedUserDetails("admin", "111111", AuthorityUtils.createAuthorityList("ROLE_PAGE_A", "PAGE_B")));
		userDetailsMap.put("xufan",
				new UnifiedUserDetails("xufan", "111111", AuthorityUtils.createAuthorityList("ROLE_PAGE_A", "PAGE_B")));

		userDetailsMap.get("admin").setDepartmentId("1");
		userDetailsMap.get("admin").setMobile("18611106983");
		userDetailsMap.get("admin").setTenantId("1");
		Map<String, List<String>> au1 = new HashMap<>();
		au1.put("ROLE1", new ArrayList<>());
		au1.get("ROLE1").add("p1");
		au1.get("ROLE1").add("p2");
		userDetailsMap.get("admin").setUserAuthorities(au1);
		Map<String, Object> payload1 = new HashMap<>();
		payload1.put("res", "res1111111");
		userDetailsMap.get("admin").setPayload(payload1);


		userDetailsMap.get("xufan").setDepartmentId("2");
		userDetailsMap.get("xufan").setMobile("18611106984");
		userDetailsMap.get("xufan").setTenantId("1");
		Map<String, List<String>> au2 = new HashMap<>();
		au2.put("ROLE2", new ArrayList<>());
		au2.get("ROLE2").add("p3");
		au2.get("ROLE2").add("p4");
		userDetailsMap.get("xufan").setUserAuthorities(au2);

		Map<String, Object> payload2 = new HashMap<>();
		payload2.put("res", "res222222");
		userDetailsMap.get("xufan").setPayload(payload2);

		return userDetailsMap.get(username);

	}

}
