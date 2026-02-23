package com.lw.graduation.api.service.notice;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lw.graduation.api.dto.notice.NoticeCreateDTO;
import com.lw.graduation.api.dto.notice.NoticePageQueryDTO;
import com.lw.graduation.api.dto.notice.NoticeUpdateDTO;
import com.lw.graduation.api.vo.notice.NoticeVO;

import java.util.List;

/**
 * 通知服务接口
 * 定义通知公告管理模块的核心业务逻辑。
 *
 * @author lw
 */
public interface NoticeService {

    /**
     * 分页查询通知列表
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    IPage<NoticeVO> getNoticePage(NoticePageQueryDTO queryDTO);

    /**
     * 根据ID获取通知详情
     *
     * @param id 通知ID
     * @return 通知详情 VO
     */
    NoticeVO getNoticeById(Long id);

    /**
     * 创建新通知
     *
     * @param createDTO 创建通知 DTO
     * @param publisherId 发布者ID
     * @return 创建的通知VO
     */
    NoticeVO createNotice(NoticeCreateDTO createDTO, Long publisherId);

    /**
     * 更新通知信息
     *
     * @param id        通知ID
     * @param updateDTO 更新通知 DTO
     * @param updaterId 更新者ID
     */
    void updateNotice(Long id, NoticeUpdateDTO updateDTO, Long updaterId);

    /**
     * 发布通知
     *
     * @param id 通知ID
     * @param publisherId 发布者ID
     */
    void publishNotice(Long id, Long publisherId);

    /**
     * 撤回通知
     *
     * @param id 通知ID
     * @param publisherId 发布者ID
     */
    void withdrawNotice(Long id, Long publisherId);

    /**
     * 删除通知
     *
     * @param id 通知ID
     * @param userId 用户ID
     */
    void deleteNotice(Long id, Long userId);

    /**
     * 获取置顶通知列表
     *
     * @param targetScope 目标范围
     * @return 置顶通知列表
     */
    List<NoticeVO> getStickyNotices(Integer targetScope);

    /**
     * 获取最新通知列表
     *
     * @param targetScope 目标范围
     * @param size 数量
     * @return 最新通知列表
     */
    List<NoticeVO> getLatestNotices(Integer targetScope, Integer size);

    /**
     * 增加通知阅读次数
     *
     * @param id 通知ID
     * @return 增加后的阅读次数
     */
    Integer increaseReadCount(Long id);
}