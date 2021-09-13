package com.phoenixhell.gulimall.member.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.phoenixhell.common.utils.PageUtils;
import com.phoenixhell.common.utils.Query;
import com.phoenixhell.gulimall.member.dao.MemberDao;
import com.phoenixhell.gulimall.member.entity.MemberEntity;
import com.phoenixhell.gulimall.member.entity.MemberLevelEntity;
import com.phoenixhell.gulimall.member.exception.PhoneExistException;
import com.phoenixhell.gulimall.member.exception.UserNameExistException;
import com.phoenixhell.gulimall.member.service.MemberLevelService;
import com.phoenixhell.gulimall.member.service.MemberService;
import com.phoenixhell.gulimall.member.vo.GithubRegistVo;
import com.phoenixhell.gulimall.member.vo.MemberLoginVo;
import com.phoenixhell.gulimall.member.vo.MemberRegistVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {
    @Autowired
    private MemberLevelService memberLevelService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public boolean checkEmailUnique(String phone) {
        return false;
    }

    @Override
    public void checkPhoneUnique(String phone) throws PhoneExistException {
        MemberEntity mobile = this.query().eq("mobile", phone).one();
        if (mobile != null) {
            throw new PhoneExistException();
        }
    }

    @Override
    public void checkUsernameUnique(String username) throws UserNameExistException {
        MemberEntity memberEntity = this.query().eq("username", username).one();
        if (memberEntity != null) {
            throw new UserNameExistException();
        }
    }


    @Override
    public void regist(MemberRegistVo vo) {
        checkUsernameUnique(vo.getUsername());
        checkPhoneUnique(vo.getPhone());
        //设置会员默认等级
        MemberEntity member = new MemberEntity();
        MemberLevelEntity memberLevelEntity = memberLevelService.query().eq("default_status", 1).one();
        member.setLevelId(memberLevelEntity.getId());
        member.setUsername(vo.getUsername());
        member.setNickname(vo.getUsername());
        member.setMobile(vo.getPhone());
        //密码加密
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String password = passwordEncoder.encode(vo.getPassword());
        member.setPassword(password);
        this.save(member);
    }

    @Override
    public MemberEntity login(MemberLoginVo vo) {
        String loginAccount = vo.getLoginAccount();
        String password = vo.getPassword();
        MemberEntity member = this.query().eq("username", loginAccount).or().eq("mobile", loginAccount).one();
        if(member==null){
            return null;
        }
        String passwordDB = member.getPassword();
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        boolean matches = passwordEncoder.matches(password, passwordDB);
        if(!matches){
            return null;
        }
        return member;
    }

    @Override
    public MemberEntity oauth2Login(GithubRegistVo vo) {
        //1 判断当前账号是否已经注册过
        MemberEntity entity = this.query().eq("github_id", vo.getGithubId()).one();
        if(entity!=null){
            entity.setPassword(null);
            return entity;
        }
        MemberEntity memberEntity = new MemberEntity();
        BeanUtils.copyProperties(vo,memberEntity);
        MemberLevelEntity memberLevelEntity = memberLevelService.query().eq("default_status", 1).one();
        memberEntity.setLevelId(memberLevelEntity.getId());
        memberEntity.setNickname(vo.getUsername());
        this.save(memberEntity);
        return memberEntity;
    }
}