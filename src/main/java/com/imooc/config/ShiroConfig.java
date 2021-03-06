package com.imooc.config;

import at.pollux.thymeleaf.shiro.dialect.ShiroDialect;
import net.sf.ehcache.CacheManager;
import org.apache.shiro.cache.ehcache.EhCacheManager;
import org.apache.shiro.codec.Base64;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.CookieRememberMeManager;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.servlet.SimpleCookie;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;
import java.util.Map;

// 用户login之后，交给SecurityManager，SecurityManager -> Authenticator(认证器，内部有认证策略) -> Realm
// 多Realm的话有三种认证策略
// AtLeastOneSuccessfulStrategy - 就算第一个成功了，realm也都会走一遍，SecurityUtils.getSubject().getPrincipal() - 会获取所有的认证成功的登录信息，如果登录名重复，只会取一个
// AllSuccessfulStrategy
// FirstSuccessfulStrategy - 返回第一个认证成功的身份信息，也认证第二个realm

@Configuration
public class ShiroConfig {
    /**
     * 会话管理器
     * @return
     */
    @Bean
    public SecurityManager securityManager() {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        securityManager.setRealm(myShiroRealm());
        securityManager.setRememberMeManager(rememberMeManager());
        securityManager.setCacheManager(getEhCacheManager());
        return securityManager;
    }

    @Bean
    public MyShiroRealm myShiroRealm() {
        MyShiroRealm myShiroRealm = new MyShiroRealm();
        myShiroRealm.setCachingEnabled(true);
        //不配置权限缓存
        myShiroRealm.setAuthorizationCachingEnabled(true);
        myShiroRealm.setAuthorizationCacheName("authorizationCache");
        return myShiroRealm;
    }

    /**
     * cookie管理对象;
     * rememberMeManager()方法是生成rememberMe管理器，而且要将这个rememberMe管理器设置到securityManager中
     * @return
     */
    @Bean
    public CookieRememberMeManager rememberMeManager(){
        CookieRememberMeManager cookieRememberMeManager = new CookieRememberMeManager();
        SimpleCookie simpleCookie = new SimpleCookie("rememberMe");
        simpleCookie.setMaxAge(60 * 60 * 24);
        cookieRememberMeManager.setCookie(simpleCookie);
        cookieRememberMeManager.setCipherKey(Base64.decode("2AvVhdsgUs0FSA3SDFAdag=="));
        return cookieRememberMeManager;
    }

    @Bean(name = "lifecycleBeanPostProcessor")
    public LifecycleBeanPostProcessor lifecycleBeanPostProcessor(){
        return new LifecycleBeanPostProcessor();
    }

    @Bean
    public EhCacheManager getEhCacheManager(){
        EhCacheManager ehcacheManager = new EhCacheManager();
        ehcacheManager.setCacheManagerConfigFile("classpath:ehcache-shiro.xml");
        return ehcacheManager;
    }

    // 登录拦截
    @Bean
    public ShiroFilterFactoryBean shiroFilter(SecurityManager securityManager) {
        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
        shiroFilterFactoryBean.setSecurityManager(securityManager);

        Map<String,String> filterChainDefinitionMap = new LinkedHashMap<>();

        // anon--匿名访问 (访问静态文件)
        filterChainDefinitionMap.put("/assets/**","anon");
        filterChainDefinitionMap.put("/imgs/**","anon");
        filterChainDefinitionMap.put("/js/**","anon");
        filterChainDefinitionMap.put("/layer/**","anon");
        filterChainDefinitionMap.put("/ueditor/**","anon");
        filterChainDefinitionMap.put("/ztree/**","anon");

        // swagger相关
        filterChainDefinitionMap.put("/swagger-ui.html","anon");
        filterChainDefinitionMap.put("/swagger-resources/**","anon");
        filterChainDefinitionMap.put("/v2/api-docs","anon");
        filterChainDefinitionMap.put("/webjars/**","anon");

        filterChainDefinitionMap.put("/user/add", "anon");
        filterChainDefinitionMap.put("/user/uploadHeadImg", "anon");
        filterChainDefinitionMap.put("/user/queryUserInfo", "anon");

        filterChainDefinitionMap.put("/card/queryListByaPage", "anon");

        // 登录相关
        filterChainDefinitionMap.put("/checkLogin","anon");
        filterChainDefinitionMap.put("/login","anon");

        // authc - 认证后可访问
        // filterChainDefinitionMap.put("/**", "authc");
        // user - 记住我或登录可访问
        filterChainDefinitionMap.put("/**", "anon");

        // 如果不设置默认会自动寻找Web工程根目录下的"/login.jsp"页面
        shiroFilterFactoryBean.setLoginUrl("/login");
        shiroFilterFactoryBean.setSuccessUrl("/");
        //未授权页面
        shiroFilterFactoryBean.setUnauthorizedUrl("/403");
        shiroFilterFactoryBean.setFilterChainDefinitionMap(filterChainDefinitionMap);
        return shiroFilterFactoryBean;
        /**
         当前登录用户: <shiro:principal/><br/>
         <shiro:authenticated>我已登录,但未记住<br/></shiro:authenticated>
         <shiro:user>我已登录,或已记住<br/></shiro:user>
         <shiro:guest>我是访客<br/></shiro:guest>
         <shiro:hasAnyRoles name="manager,admin">manager or admin 角色用户登录显示此内容<br/></shiro:hasAnyRoles>
         <shiro:hasRole name="系统管理员">我是系统管理员<br/></shiro:hasRole>
         <h2>权限列表</h2>
         <shiro:hasPermission name="权限列表">具有权限列表权限用户显示此内容<br/></shiro:hasPermission>
         <shiro:lacksPermission name="角色分配保存">不具有角色分配保存权限的用户显示此内容 <br/></shiro:lacksPermission>
         */
    }

    /**
     * thymeleaf里使用shiro的标签的bean
     * @return
     */
    @Bean
    public ShiroDialect shiroDialect() {
        return new ShiroDialect();
    }
}
