package com.example.demo;

import com.example.primary.User;
import com.example.primary.UserRepository;
import com.example.slave.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.PreparedStatement;
import java.sql.Statement;

/**
 * @author qiuwm.
 * @version 1.00.00
 * @date 2019/5/23.
 */
@RestController
public class UserController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private com.example.slave.UserRepository slaveUserRepository;

    @Autowired
    private com.example.primary.UserMapper userMapper;

    @Autowired
    private UserMapper slaveUserMapper;

    @RequestMapping("/findAll")
    public Object findAll(){
        return userRepository.findAll();
    }

    @RequestMapping("/findAll2")
    public Object findAll2(){
        return slaveUserRepository.findAll();
    }

    @Qualifier("primaryJdbcTemplate")
    @Autowired
    private JdbcTemplate primaryJdbcTemplate;

    @Autowired
    @Qualifier("slaveJdbcTemplate")
    private JdbcTemplate slaveJdbcTemplate;

    @RequestMapping("/save1")
    public Object save1(String name){
        KeyHolder keyHolder = new GeneratedKeyHolder();
        primaryJdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement("insert into user(name) values(?)", Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, name);
            return ps;
        }, keyHolder);

        Long id = keyHolder.getKey().longValue();
        User user = new User();
        user.setId(id);
        user.setName(name);
        return user;
    }

    @RequestMapping("/save2")
    public Object save2(String name){
        KeyHolder keyHolder = new GeneratedKeyHolder();
        slaveJdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement("insert into user(name) values(?)", Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, name);
            return ps;
        }, keyHolder);

        Long id = keyHolder.getKey().longValue();
        com.example.slave.User user = new com.example.slave.User();
        user.setId(id);
        user.setName(name);
        return user;
    }

    @RequestMapping("update1")
    public Object update1(Long id, String name){
        int effect = userMapper.update(id, name);
        return effect==1;
    }

    @RequestMapping("update2")
    public Object update2(Long id, String name){
        int effect = slaveUserMapper.update(id, name);
        return effect==1;
    }
}
