package com.sky.mapper;


import com.sky.dto.UserReportDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface UserMapper {

    /**
     * 根据openid获取用户信息
     * @param openid
     * @return
     */
    @Select("select id, openid, name, phone, sex, id_number, avatar, create_time from user where openid = #{openid}")
    User selectByOpenid(String openid);

    @Insert("insert into user(openid, name, phone, sex, id_number, avatar, create_time)" +
            "values (#{openid},#{name},#{phone},#{sex},#{idNumber},#{avatar},#{createTime})")
    @Options(useGeneratedKeys = true, keyColumn = "id", keyProperty = "id")
    void insert(User user);

    @Select("select ID, OPENID, NAME, PHONE, SEX, ID_NUMBER, AVATAR, CREATE_TIME from user where id = #{userId}")
    User getById(Long userId);


    /**
     * 统计每一天的新增用户数
     * @param beginTime
     * @param endTime
     * @return
     */
    List<UserReportDTO> countAndByCreateTime(LocalDateTime beginTime, LocalDateTime endTime);


    /**
     * 统计截止到指定时间的用户数量
     * @param beginTime
     * @return
     */
    @Select("select count(*) from user where create_time < #{beginTime}")
    Integer countTotalByCreateTime(LocalDateTime beginTime);


    Integer countByTime(LocalDateTime beginTime, LocalDateTime endTime);
}
