package com.example.filter;

import com.example.entity.RestBean;
import com.example.utils.Const;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

//用于限流用户的恶意压测等操作
//用redis保存用户3s内请求的次数,保存到Const.FLOW_LIMIT_COUNTER + ip里
//若次数超过10次,用redis存放限流的黑名单(Const.FLOW_LIMIT_BLOCK + ip)30s
//在黑名单里的ip直接返回数据
@Component
@Order(Const.ORDER_LIMIT)
public class FlowLimitFilter extends HttpFilter {
    @Resource
    StringRedisTemplate stringRedisTemplate;
    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        String ip = request.getRemoteAddr();
        if(this.tryCount(ip)){
            chain.doFilter(request,response);
        } else {
            this.writeBlockMessage(response);
        }
    }
    //尝试计算ip请求的次数
    private boolean tryCount(String ip) {
        //依旧是要加把锁,因为如果不加锁,用户1s内请求1000次
        //而这1000次又都同时操作的话。
        //你想想就知道,这1000次肯定都通过了,而我们的目的是只能最多3s内访问10次,就没了呗
        synchronized (ip.intern()){
            //在黑名单里直接返回false
            if(Boolean.TRUE.equals(stringRedisTemplate.hasKey(Const.FLOW_LIMIT_BLOCK + ip))){
                return false;
            }
            return this.limitPeriodCheck(ip);
        }
    }
    private boolean limitPeriodCheck(String ip){
        String key = Const.FLOW_LIMIT_COUNTER + ip;
        //判断是否已经在redis里计数了
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(key))) {
            //增加计数
            //有可能redis里刚好到期返回null,就用Optional包裹就行了,orElse就是如果是空就返回0,不是就原样返回
            Long increment = Optional.ofNullable(stringRedisTemplate.opsForValue().increment(key)).orElse(0L);
            if(increment > 10){
                //计数大于10,放到黑名单里
                stringRedisTemplate.opsForValue().set(Const.FLOW_LIMIT_BLOCK + ip, "",30,TimeUnit.SECONDS);
                return false;
            }
        } else {
            stringRedisTemplate.opsForValue().set(key, "1",3, TimeUnit.SECONDS);
        }
        return true;
    }
    private void writeBlockMessage(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=utf-8");
        response.getWriter().write(RestBean.forbidden("操作频繁").asJsonString());
    }
}
