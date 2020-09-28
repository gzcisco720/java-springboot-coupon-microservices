package com.spring.coupon.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "coupon_path")
public class Path implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "path_pattern", nullable = false)
    private String pathPattern;

    @Column(name = "http_method", nullable = false)
    private String httpMethod;

    @Column(name = "path_name", nullable = false)
    private String pathName;

    @Column(name = "service_name", nullable = false)
    private String serviceName;

    @Column(name = "op_mode", nullable = false)
    private String opMode;

    public Path( String pathPattern, String httpMethod, String pathName,
                 String serviceName, String opMode) {
        this.pathPattern = pathPattern;
        this.httpMethod = httpMethod;
        this.pathName = pathName;
        this.serviceName = serviceName;
        this.opMode = opMode;
    }
}
