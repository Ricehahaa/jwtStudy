package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.dto.Account;
import com.example.entity.vo.request.ConfirmResetVO;
import com.example.entity.vo.request.EmailRegisterVO;
import com.example.entity.vo.request.EmailResetVO;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface AccountService extends IService<Account>, UserDetailsService {
    Account findAccountByNameOrEmail(String text);

    //获取注册验证码
    String registerEmailVerifyCode(String type, String email, String ip);

    String registerEmailAccount(EmailRegisterVO vo);

    String resetConfirm(ConfirmResetVO vo);
    String  resetEmailAccountPassword(EmailResetVO vo);
}
