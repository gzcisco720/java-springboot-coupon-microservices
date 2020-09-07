package com.spring.cloud.service.impl;

import com.spring.cloud.dao.CouponTemplateDao;
import com.spring.cloud.dto.TemplateRequest;
import com.spring.cloud.entity.CouponTemplate;
import com.spring.cloud.exception.CouponException;
import com.spring.cloud.service.IAsyncService;
import com.spring.cloud.service.IBuildTemplateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class BuildTemplateServiceImpl implements IBuildTemplateService {
    private final CouponTemplateDao templateDao;
    private final IAsyncService asyncService;

    public BuildTemplateServiceImpl(CouponTemplateDao templateDao, IAsyncService asyncService) {
        this.templateDao = templateDao;
        this.asyncService = asyncService;
    }

    @Override
    public CouponTemplate buildTemplate(TemplateRequest request) throws CouponException {
        if (!request.validate()) {
            throw new CouponException("buildTemplate parameter is not valid");
        }
        if (null != templateDao.findByName(request.getName())) {
            throw new CouponException("template already exists");
        }
        CouponTemplate template = requestToCouponTemplate(request);
        template = templateDao.save(template);
        asyncService.asyncConstructCouponByTemplate(template);
        return template;
    }

    private CouponTemplate requestToCouponTemplate(TemplateRequest request) {
        return new CouponTemplate(
                request.getName(),
                request.getLogo(),
                request.getDesc(),
                request.getCategory(),
                request.getProductLine(),
                request.getCount(),
                request.getUserId(),
                request.getTarget(),
                request.getRule()
        );
    }
}
