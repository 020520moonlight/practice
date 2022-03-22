package fm.douban.app.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import fm.douban.app.interceptor.UserInterceptor;

@Configuration
public class AppConfigurer implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 多个拦截器组成一个拦截器链
        // 仅演示，设置所有 url 都拦截
        registry.addInterceptor(new UserInterceptor()).addPathPatterns("/my")
            .addPathPatterns("/fav");
    }
}
