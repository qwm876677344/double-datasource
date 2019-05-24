package com.example.primary;

import lombok.Data;

import javax.persistence.*;

/**
 * @author qiuwm.
 * @version 1.00.00
 * @date 2019/5/23.
 */
@Entity
@Data
@Table(name = "user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String name;
}
