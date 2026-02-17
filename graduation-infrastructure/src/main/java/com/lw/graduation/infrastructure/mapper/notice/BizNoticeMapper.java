package com.lw.graduation.infrastructure.mapper.notice;

import com.lw.graduation.domain.entity.notice.BizNotice;
import com.lw.graduation.infrastructure.mapper.MyBaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 通知Mapper接口
 *
 * @author lw
 */
@Mapper
public interface BizNoticeMapper extends MyBaseMapper<BizNotice> {

}