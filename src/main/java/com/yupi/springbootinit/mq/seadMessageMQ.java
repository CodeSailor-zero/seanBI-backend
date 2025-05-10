package com.yupi.springbootinit.mq;

import lombok.Builder;
import lombok.Data;

/**
 * @author Sean
 * @version 1.0
 * &#064;date 2025/3/12
 **/
@Data
public class seadMessageMQ {
    Long id;
    String aiType;
    String userRole;
}
