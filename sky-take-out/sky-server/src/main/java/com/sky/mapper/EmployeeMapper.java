package com.sky.mapper;

import com.sky.entity.Employee;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;


@Mapper
public interface EmployeeMapper {

    /**
     * 根据用户名查询员工信息
     * @param username
     * @return
     */
    @Select("select id, name, username, password, phone, sex, id_number, status, create_time, update_time, create_user, update_user from employee where username = #{username}")
    public Employee findByUsername(String username);


    @Insert("insert into employee(name, username, password, phone, sex, id_number, status, create_time, update_time, create_user, update_user) values (#{name},#{username},#{password},#{phone},#{sex},#{idNumber},#{status},#{createTime},#{updateTime},#{createUser},#{updateUser})")
    void save(Employee employee);


    /**
     * 动态条件查询
     * @param name
     * @return
     */
     List<Employee> list(String name);

     void update(Employee employee);
}
