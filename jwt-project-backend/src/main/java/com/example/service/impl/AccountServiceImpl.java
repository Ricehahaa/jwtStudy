package com.example.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dto.Account;
import com.example.entity.vo.request.ConfirmResetVO;
import com.example.entity.vo.request.EmailRegisterVO;
import com.example.entity.vo.request.EmailResetVO;
import com.example.mapper.AccountMapper;
import com.example.service.AccountService;
import com.example.utils.Const;
import com.example.utils.FlowUtils;
import jakarta.annotation.Resource;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.Wrapper;
import java.util.Date;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class AccountServiceImpl extends ServiceImpl<AccountMapper, Account> implements AccountService {

    @Resource
    AmqpTemplate amqpTemplate;

    @Resource
    StringRedisTemplate stringRedisTemplate;

    @Resource
    FlowUtils flowUtils;


    @Resource
    BCryptPasswordEncoder encoder;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = findAccountByNameOrEmail(username);
        if(account == null)
            throw new UsernameNotFoundException("用户名错误");

        return User
                .withUsername(username)
                .password(account.getPassword())
                .roles(account.getRole())
                .build();
    }

    public Account findAccountByNameOrEmail(String text){
        return this.query()
                .eq("username",text).or()
                .eq("email", text)
                .one();
    }

    @Override
    public String registerEmailVerifyCode(String type, String email, String ip) {
        //加锁是因为,如果同一时间内用户发来1000次请求,那么就都进发验证码了,redis限流形同虚设
        synchronized (ip.intern()) {
            if (!this.verifyLimit(ip))
                return "请求频繁,请稍后再试";
            Random random = new Random();
            int code = random.nextInt(900000) + 100000;
            Map<String, Object> data = Map.of("type", type, "email", email, "code", code);
            //直接发给消息队列,进行发送邮件
            amqpTemplate.convertAndSend("mail", data);
            //放在redis里,用于注册时判断验证码正确与否,3分钟过期
            stringRedisTemplate.opsForValue()
                    .set(Const.VERIFY_EMAIL_DATA + email, String.valueOf(code), 3, TimeUnit.MINUTES);
            return null;
        }
    }

    @Override
    public String registerEmailAccount(EmailRegisterVO vo) {
        String email = vo.getEmail();
        String key = Const.VERIFY_EMAIL_DATA + email;
        String code = stringRedisTemplate.opsForValue().get(key);
        String username = vo.getUsername();
        if(code == null) return "请先获取验证码";
        if(!code.equals(vo.getCode())) return "验证码错误";
        if(this.existsAccountByEmail(email)) return "电子邮件已被注册";
        if(this.existsAccountByUsername(username)) return "用户名已被注册";
        String password = encoder.encode(vo.getPassword());
        Account account = new Account(null, username, password, email, "user",new Date());
        if (this.save(account)) {
            stringRedisTemplate.delete(key);
            return null;
        }
        return "数据库内部错误";
    }

    @Override
    public String resetConfirm(ConfirmResetVO vo) {
        String email = vo.getEmail();
        String code = stringRedisTemplate.opsForValue().get(Const.VERIFY_EMAIL_DATA + email);
        if(code == null) return "请先获取验证码";
        if(!code.equals(vo.getCode())) return "验证码错误";
        return null;
    }

    @Override
    public String resetEmailAccountPassword(EmailResetVO vo) {
        String email = vo.getEmail();
        String s = this.resetConfirm(new ConfirmResetVO(email, vo.getCode()));
        if(s != null) return s;
        String password = encoder.encode(vo.getPassword());
        if(this.update().eq("email", email).set("password",password).update()) {
            stringRedisTemplate.delete(Const.VERIFY_EMAIL_DATA + email);
        }
        return null;
    }

    //通过邮件判断用户是否存在
    private boolean existsAccountByEmail(String email){
        return this.baseMapper.exists(Wrappers.<Account>query().eq("email", email));
    }

    //通过邮件判断用户是否存在
    private boolean existsAccountByUsername(String username){
        return this.baseMapper.exists(Wrappers.<Account>query().eq("username", username));
    }
    //防止用户请求过度,用redis限流60s
    private boolean verifyLimit(String ip){
        String key = Const.VERIFY_EMAIL_LIMIT + ip;
        return flowUtils.limitOnceCheck(key, 60);
    }
}
