package com.example.oldguy.modules.modeler.security;

import org.flowable.ui.common.security.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @ClassName: MyFilter
 * @Author: huangrenhao
 * @Description:
 * @CreateTime： 2020/1/19 0019 下午 4:04
 * @Version：
 **/
@Component
@WebFilter(urlPatterns = {"/app/**", "/api/**"})
public class MyFilter extends OncePerRequestFilter {

    private Logger LOGGER = LoggerFactory.getLogger(MyFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        if (skipAuthenticationCheck(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        LOGGER.debug("MyFilter:doFilterInternal:" + request.getRequestURL());

        if (StringUtils.isEmpty(SecurityUtils.getCurrentUserId())) {

            LOGGER.debug("MyFilter:doFilterInternal:校验......");
            UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken("admin", "");
            SecurityContextHolder.getContext().setAuthentication(token);

        } else {
            LOGGER.debug("MyFilter:doFilterInternal:校验通过.......");
        }

        filterChain.doFilter(request, response);
    }

    protected boolean skipAuthenticationCheck(HttpServletRequest request) {

        if (request.getRequestURI().endsWith("/doc.html")){
            LOGGER.info("swagger处理");
            return false;
        }

        return request.getRequestURI().endsWith(".css") ||
                request.getRequestURI().endsWith(".js") ||
                request.getRequestURI().endsWith(".html") ||
                request.getRequestURI().endsWith(".map") ||
                request.getRequestURI().endsWith(".woff") ||
                request.getRequestURI().endsWith(".png") ||
                request.getRequestURI().endsWith(".jpg") ||
                request.getRequestURI().endsWith(".jpeg") ||
                request.getRequestURI().endsWith(".tif") ||
                request.getRequestURI().endsWith(".tiff");
    }

}
