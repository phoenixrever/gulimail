package com.phoenixhell.gulimall.member.dao;

import com.phoenixhell.gulimall.member.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员
 * 
 * @author phoenixhell
 * @email phoenixrever@gmail.com
 * @date 2021-05-18 22:34:06
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {
	
}
