package com.spring.coupon.service;

import com.spring.coupon.dao.PathRepository;
import com.spring.coupon.entity.Path;
import com.spring.coupon.vo.CreatePathRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PathService {

    private final PathRepository repository;

    public PathService(PathRepository repository) {
        this.repository = repository;
    }

    public List<Integer> createPath(CreatePathRequest request) {
        List<CreatePathRequest.PathInfo> infos = request.getPathInfos();
        List<CreatePathRequest.PathInfo> validRequests = new ArrayList<>(request.getPathInfos().size());
        List<Path> currentPath = repository.findAllByServiceName(
                infos.get(0).getServiceName()
        );
        if (!CollectionUtils.isEmpty(currentPath)) {
            for (CreatePathRequest.PathInfo info : infos) {
                boolean isValid = true;
                for (Path path : currentPath) {
                    if (path.getPathPattern().equals(info.getPathPattern()) &&
                    path.getHttpMethod().equals(info.getHttpMethod())) {
                        isValid = false;
                        break;
                    }
                }
                if (isValid) {
                    validRequests.add(info);
                }
            }
        } else {
            validRequests = infos;
        }
        List<Path> paths = new ArrayList<>(validRequests.size());
        validRequests.forEach(p -> paths.add(new Path(
                p.getPathPattern(),
                p.getHttpMethod(),
                p.getPathName(),
                p.getServiceName(),
                p.getOpMode()
        )));
        return repository.saveAll(paths).stream()
                .map(Path::getId).collect(Collectors.toList());
    }
}
