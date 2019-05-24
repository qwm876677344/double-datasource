package com.example.primary;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * @author qiuwm
 * @version v1.0
 * @Description TODO功能描述
 * @date 2019/5/24.
 */
public interface UserMapper {
    @Update("update user set name = #{name} where id = #{id}")
    int update(@Param("id") Long id, @Param("name") String name);
}
