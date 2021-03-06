package com.thelittlegym.mobile.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.thelittlegym.mobile.Convertor.DateConvert;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by TONY on 2017/10/6.
 */


@Data
@Entity
@Table(name="activity_enrollment")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ActivityEnrollment {
    @Id
    @GeneratedValue
    private Integer id;
    private Date createTime;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", nullable = true)
    private User user;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activityId", nullable = true)
    private Activity activity;
    //1：报名；0：取消；3：预报名
    private  Integer status;
}
