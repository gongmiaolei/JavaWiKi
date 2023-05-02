package com.jiawa.wiki.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jiawa.wiki.domain.Admin;
import com.jiawa.wiki.domain.AdminExample;
import com.jiawa.wiki.exception.BusinessException;
import com.jiawa.wiki.exception.BusinessExceptionCode;
import com.jiawa.wiki.mapper.AdminMapper;
import com.jiawa.wiki.req.AdminLoginReq;
import com.jiawa.wiki.req.AdminQueryReq;
import com.jiawa.wiki.req.AdminResetPasswordReq;
import com.jiawa.wiki.req.AdminSaveReq;
import com.jiawa.wiki.resp.PageResp;
import com.jiawa.wiki.resp.AdminLoginResp;
import com.jiawa.wiki.resp.AdminQueryResp;
import com.jiawa.wiki.util.CopyUtil;
import com.jiawa.wiki.util.SnowFlake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.List;

@Service
public class AdminService {

    private static final Logger LOG = LoggerFactory.getLogger(AdminService.class);

    @Resource
    private AdminMapper adminMapper;

    @Resource
    private SnowFlake snowFlake;

    public PageResp<AdminQueryResp> list(AdminQueryReq req) {
        AdminExample adminExample = new AdminExample();
        AdminExample.Criteria criteria = adminExample.createCriteria();
        if (!ObjectUtils.isEmpty(req.getLoginName())) {
            criteria.andLoginNameEqualTo(req.getLoginName());
        }
        PageHelper.startPage(req.getPage(), req.getSize());
        List<Admin> adminList = adminMapper.selectByExample(adminExample);

        PageInfo<Admin> pageInfo = new PageInfo<>(adminList);
        LOG.info("总行数：{}", pageInfo.getTotal());
        LOG.info("总页数：{}", pageInfo.getPages());

        // List<AdminResp> respList = new ArrayList<>();
        // for (Admin admin : adminList) {
        //     // AdminResp adminResp = new AdminResp();
        //     // BeanUtils.copyProperties(admin, adminResp);
        //     // 对象复制
        //     AdminResp adminResp = CopyUtil.copy(admin, AdminResp.class);
        //
        //     respList.add(adminResp);
        // }

        // 列表复制
        List<AdminQueryResp> list = CopyUtil.copyList(adminList, AdminQueryResp.class);

        PageResp<AdminQueryResp> pageResp = new PageResp();
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(list);

        return pageResp;
    }

    /**
     * 保存
     */
    public void save(AdminSaveReq req) {
        Admin admin = CopyUtil.copy(req, Admin.class);
        if (ObjectUtils.isEmpty(req.getId())) {
            Admin adminDB = selectByLoginName(req.getLoginName());
            if (ObjectUtils.isEmpty(adminDB)) {
                // 新增
                admin.setId(snowFlake.nextId());
                adminMapper.insert(admin);
            } else {
                // 用户名已存在
                throw new BusinessException(BusinessExceptionCode.USER_LOGIN_NAME_EXIST);
            }
        } else {
            // 更新
            admin.setLoginName(null);
            admin.setPassword(null);
            adminMapper.updateByPrimaryKeySelective(admin);
        }
    }

    public void delete(Long id) {
        adminMapper.deleteByPrimaryKey(id);
    }

    public Admin selectByLoginName(String LoginName) {
        AdminExample adminExample = new AdminExample();
        AdminExample.Criteria criteria = adminExample.createCriteria();
        criteria.andLoginNameEqualTo(LoginName);
        List<Admin> adminList = adminMapper.selectByExample(adminExample);
        if (CollectionUtils.isEmpty(adminList)) {
            return null;
        } else {
            return adminList.get(0);
        }
    }

    /**
     * 修改密码
     */
    public void resetPassword(AdminResetPasswordReq req) {
        Admin admin = CopyUtil.copy(req, Admin.class);
        adminMapper.updateByPrimaryKeySelective(admin);
    }

    /**
     * 登录
     */
    public AdminLoginResp login(AdminLoginReq req) {
        Admin adminDb = selectByLoginName(req.getLoginName());
        if (ObjectUtils.isEmpty(adminDb)) {
            // 用户名不存在
            LOG.info("用户名不存在, {}", req.getLoginName());
            throw new BusinessException(BusinessExceptionCode.LOGIN_USER_ERROR);
        } else {
            if (adminDb.getPassword().equals(req.getPassword())) {
                // 登录成功
                AdminLoginResp adminLoginResp = CopyUtil.copy(adminDb, AdminLoginResp.class);
                return adminLoginResp;
            } else {
                // 密码不对
                LOG.info("密码不对, 输入密码：{}, 数据库密码：{}", req.getPassword(), adminDb.getPassword());
                throw new BusinessException(BusinessExceptionCode.LOGIN_USER_ERROR);
            }
        }
    }
}
