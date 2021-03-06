package com.thelittlegym.mobile.entity;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;

import javax.persistence.*;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Created by hibernate on 2017/5/17.
 */
@Entity
@Data
@Table(name="activity")
public class Activity {
    @Id
    @GeneratedValue
    private Integer id;
    @Column(name = "createTime")
    private Date createTime;
    private String name;
    private String detail;
    @Column(name = "beginDate")
    private Date beginDate;
    @Column(name = "endDate")
    private Date endDate;
    private String type;
    //收费总类
    @Column(name = "chargeType")
    private String chargeType;
    //人群
    private String crowd;
    //强度
    private String strength;
    @Column(name = "isDelete")
    private Boolean isDelete;
    @Column(name = "bannerSrc")
    private String bannerSrc;
    private String city;
    private String search;

    @Override
    public String toString() {
        return "|"+ city + "|"+ name + "|"
                +detail + "|"+beginDate;
    }
}
