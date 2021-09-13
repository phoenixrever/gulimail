package com.phoenixhell.gulimall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.phoenixhell.common.utils.PageUtils;
import com.phoenixhell.gulimall.member.entity.MemberEntity;
import com.phoenixhell.gulimall.member.exception.PhoneExistException;
import com.phoenixhell.gulimall.member.exception.UserNameExistException;
import com.phoenixhell.gulimall.member.vo.GithubRegistVo;
import com.phoenixhell.gulimall.member.vo.MemberLoginVo;
import com.phoenixhell.gulimall.member.vo.MemberRegistVo;

import java.util.Map;

/**
 * 会员
 *
 * @author phoenixhell
 * @email phoenixrever@gmail.com
 * @date 2021-05-18 22:34:06
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void checkPhoneUnique(String phone) throws PhoneExistException;

    void checkUsernameUnique(String phone) throws UserNameExistException;

    boolean checkEmailUnique(String phone);

    void regist(MemberRegistVo vo);

    MemberEntity login(MemberLoginVo vo);

    MemberEntity oauth2Login(GithubRegistVo vo);
}

