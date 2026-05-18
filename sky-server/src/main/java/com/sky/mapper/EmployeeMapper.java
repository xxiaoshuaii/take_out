package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.Annoation.AuotiFill;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.enumeration.OperationType;
import com.sky.result.PageResult;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface EmployeeMapper {

    /**
     * 根据用户名查询员工
     * @param username
     * @return
     */
    @Select("select * from employee where username = #{username}")
    Employee getByUsername(String username);

    /**
     * 插入员工数据
     * @param employee
     */
    @AuotiFill(OperationType.INSERT)
    void save(Employee employee);

    /**
     * 分页查询
     * @param page
     * @return
     */
    Page<Employee> pageQuery(EmployeePageQueryDTO page);

    /**
     * 根据id查询状态码
     * @param employee
     * @return
     */
    @AuotiFill(OperationType.UPDATE)
    void Update(Employee employee);

    /**
     * 根据id查询员工信息
     * @return
     */
    @Select("select * from employee where id = #{id}")
    Employee getById(Long id);
}
