package com.phoenixhell.gulimall.member.controller;

import com.phoenixhell.common.exception.BizCodeEnume;
import com.phoenixhell.common.utils.PageUtils;
import com.phoenixhell.common.utils.R;
import com.phoenixhell.gulimall.member.entity.MemberEntity;
import com.phoenixhell.gulimall.member.exception.PhoneExistException;
import com.phoenixhell.gulimall.member.exception.UserNameExistException;
import com.phoenixhell.gulimall.member.feign.CouponFeignService;
import com.phoenixhell.gulimall.member.service.MemberReceiveAddressService;
import com.phoenixhell.gulimall.member.service.MemberService;
import com.phoenixhell.gulimall.member.vo.GithubRegistVo;
import com.phoenixhell.gulimall.member.vo.MemberLoginVo;
import com.phoenixhell.gulimall.member.vo.MemberRegistVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;


/**
 * 会员
 *
 * @author phoenixhell
 * @email phoenixrever@gmail.com
 * @date 2021-05-18 22:34:06
 */
@RestController
@RequestMapping("member/member")
public class MemberController {
    @Autowired
    private MemberService memberService;

    @Autowired
    private CouponFeignService couponFeignService;

    @Autowired
    private MemberReceiveAddressService memberReceiveAddressService;

    @GetMapping("/coupon")
    public R test() {
        MemberEntity memberEntity = new MemberEntity();
        memberEntity.setNickname("phoenixhell");
        R r = couponFeignService.memberCoupons();
        return R.ok().put("member", memberEntity).put("coupon", r);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = memberService.queryPage(params);

        return R.ok().put("page", page);
    }

    /**
     * feign github登录
     */
    @PostMapping("/oauth2Login")
    public R oauth2Login(@RequestBody GithubRegistVo vo) {
           MemberEntity memberEntity= memberService.oauth2Login(vo);
        return R.ok().put("data",memberEntity);
    }

    /**
     * feign 注册用户
     */
    @PostMapping("/regist")
    public R regist(@RequestBody MemberRegistVo vo) {
        try {
            memberService.regist(vo);
        } catch (UserNameExistException e) {
            return R.error(BizCodeEnume.USER_EXIST_EXCEPTION.getCode(), BizCodeEnume.USER_EXIST_EXCEPTION.getMsg());
        } catch (PhoneExistException e) {
            return R.error(BizCodeEnume.PHONE_EXIST_EXCEPTION.getCode(), BizCodeEnume.PHONE_EXIST_EXCEPTION.getMsg());
        }
        return R.ok();
    }

    /**
     * feign 登录用户
     */
    @PostMapping("/login")
    public R login(@RequestBody MemberLoginVo vo) {
       MemberEntity memberEntity = memberService.login(vo);
       if(memberEntity==null){
           return R.error(BizCodeEnume.LOGINACCOUNT_PASSWORD_EXCEPTION.getCode(),BizCodeEnume.LOGINACCOUNT_PASSWORD_EXCEPTION.getMsg());
       }
        return R.ok().put("data",memberEntity);
    }

    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id) {
        MemberEntity member = memberService.getById(id);

        return R.ok().put("member", member);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody MemberEntity member) {
        memberService.save(member);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody MemberEntity member) {
        memberService.updateById(member);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids) {
        memberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
