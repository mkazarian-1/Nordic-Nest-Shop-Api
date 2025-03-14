package org.example.nordicnestshop.config;

import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        PageableHandlerMethodArgumentResolver resolver
                = new PageableHandlerMethodArgumentResolver();
        resolver.setSizeParameterName("page_size"); // Use "pageSize" instead of "size"
        resolver.setPageParameterName("page_number"); // Optional: Customize the page parameter name
        resolvers.add(resolver);
    }
}
