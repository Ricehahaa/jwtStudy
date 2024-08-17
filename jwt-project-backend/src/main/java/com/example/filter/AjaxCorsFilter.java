package com.example.filter;

import com.example.utils.Const;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
//需要放在security前
@Order(Const.ORDER_CORS)
//注意不能命名成CorsFilter
public class AjaxCorsFilter extends HttpFilter {
    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {

        this.addCorsHeader(request, response);
        chain.doFilter(request, response);
    }
    private void addCorsHeader(HttpServletRequest request, HttpServletResponse response) {
        //request.getHeader("Origin")是获得发请求的原始站点
        //这一步操作就是让所有地方的请求都可以通过
        response.addHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));
        //可以自己限制
//        response.addHeader("Access-Control-Allow-Origin", "http://localhost:5173");
        //运行跨域的方法
        response.addHeader("Access-Control-Allow-Methods","GET, POST, PUT, DELETE");
        response.addHeader("Access-Control-Allow-Headers", "Authorization, Content-Type");
    }
}
