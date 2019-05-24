package com.example.demo;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateSettings;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

/**
 * @author qiuwm.
 * @version 1.00.00
 * @date 2019/5/23.
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        entityManagerFactoryRef = "primaryEntityManagerFactory",
        transactionManagerRef = "primaryTransactionManager",
        //TODO 注意修改为自己的primary的repository路径
        basePackages = {"com.example.primary"})
@MapperScan(basePackages = "com.example.primary", sqlSessionTemplateRef = "primarySqlSessionTemplate")
public class PrimaryConfig {

    @Autowired
    private JpaProperties jpaProperties;

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource.primary")
    public DataSourceProperties primaryDataSourceProperties() {
        return new DataSourceProperties();
    }


    @Primary
    @Bean(name = "primaryDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.primary.configuration")
    // 【2】datasource配置的前缀，对应上面 【mysql的yaml配置】
    public DataSource dataSource() {
        return primaryDataSourceProperties().initializeDataSourceBuilder()
                .type(HikariDataSource.class).build();
    }


    @Bean(name = "primaryJdbcTemplate")
    @Primary
    public JdbcTemplate primaryJdbcTemplate(
            @Qualifier("primaryDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    /**================jpa多数据源配置==============**/
    @Primary
    @Bean(name = "primaryEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean
    entityManagerFactory(
            EntityManagerFactoryBuilder builder, @Qualifier("primaryDataSource") DataSource dataSource
    ) {
        return builder
                .dataSource(dataSource)
                .packages("com.example.primary") // 【3】这里是实体类的包路径
                .persistenceUnit("primary") // 这里写成唯一的就可以了，具体我也不太明白 ，希望有人告知
                // 【4】
                .properties(jpaProperties.getHibernateProperties(new HibernateSettings()))
                .build();
    }

    @Primary
    @Bean(name = "primaryTransactionManager")
    public PlatformTransactionManager transactionManager(
            @Qualifier("primaryEntityManagerFactory") EntityManagerFactory
                    entityManagerFactory
    ) {
        return new JpaTransactionManager(entityManagerFactory);
    }


    /**=============mybatis多数据源配置==============**/
    /**
     * 创建工厂
     *
     * @param dataSource
     * @return SqlSessionFactory
     * @throws Exception
     */
    @Bean(name = "primarySqlSessionFactory")
    @Primary
    public SqlSessionFactory masterSqlSessionFactory(@Qualifier("primaryDataSource") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(dataSource);
        bean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath:mapper/master/**/*.xml"));
        return bean.getObject();
    }

    /**
     * 创建事务
     *
     * @param dataSource
     * @return DataSourceTransactionManager
     */
    @Bean(name = "primaryTransactionManager")
    @Primary
    public DataSourceTransactionManager masterDataSourceTransactionManager(@Qualifier("primaryDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    /**
     * 创建模板
     *
     * @param sqlSessionFactory
     * @return SqlSessionTemplate
     */
    @Bean(name = "primarySqlSessionTemplate")
    @Primary
    public SqlSessionTemplate masterSqlSessionTemplate(@Qualifier("primarySqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

}
