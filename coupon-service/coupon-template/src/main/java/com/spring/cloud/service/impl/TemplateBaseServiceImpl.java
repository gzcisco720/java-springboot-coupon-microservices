package com.spring.cloud.service.impl;

import com.spring.cloud.dao.CouponTemplateDao;
import com.spring.cloud.entity.CouponTemplate;
import com.spring.cloud.exception.CouponException;
import com.spring.cloud.service.ITemplateBaseService;
import com.spring.cloud.vo.CouponTemplateSDK;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TemplateBaseServiceImpl implements ITemplateBaseService {

    private final CouponTemplateDao templateDao;

    public TemplateBaseServiceImpl(CouponTemplateDao templateDao) {
        this.templateDao = templateDao;
    }

    @Override
    public CouponTemplate buildTemplateInfo(Integer id)
            throws CouponException {
        Optional<CouponTemplate> template = templateDao.findById(id);
        if (!template.isPresent()) {
            throw new CouponException("Template does not exist");
        }
        return template.get();
    }

    @Override
    public List<CouponTemplateSDK> findAllUsableTemplate() {
        List<CouponTemplate> templates = templateDao.findAllByAvailableAndExpired(true, false);
        return templates.stream().map(this::template2TemplateSDK).collect(Collectors.toList());
    }

    @Override
    public Map<Integer, CouponTemplateSDK> findIds2TemplateSDK(Collection<Integer> ids) {
        List<CouponTemplate> templates = templateDao.findAllById(ids);

        return templates.stream().map(this::template2TemplateSDK).collect(Collectors.toMap(
                CouponTemplateSDK::getId, Function.identity()
        ));
    }

    private CouponTemplateSDK template2TemplateSDK(CouponTemplate template) {
        return new CouponTemplateSDK(
                template.getId(),
                template.getName(),
                template.getLogo(),
                template.getDesc(),
                template.getCategory().getCode(),
                template.getProductLine().getCode(),
                template.getKey(),
                template.getTarget().getCode(),
                template.getRule()
        );
    }
}
