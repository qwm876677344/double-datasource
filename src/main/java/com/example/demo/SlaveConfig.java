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
        entityManagerFactoryRef = "slaveEntityManagerFactory",
        transactionManagerRef = "slaveTransactionManager",
        basePackages = {"com.example.slave"})
@MapperScan(basePackages = "com.example.slave", sqlSessionTemplateRef = "slaveSqlSessionTemplate")
public class SlaveConfig {

    @Autowired
    private JpaProperties jpaProperties;

    @Bean
    @ConfigurationProperties("spring.datasource.slave")
    public DataSourceProperties slaveDataSourceProperties() {
        return new DataSourceProperties();
    }


    @Bean(name = "slaveDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.slave.configuration") // 【2】datasource配置的前缀，对应上面 【mysql的yaml配置】
    public DataSource dataSource() {
        return slaveDataSourceProperties().initializeDataSourceBuilder()
                .type(HikariDataSource.class).build();
    }

    @Bean(name = "slaveJdbcTemplate")
    public JdbcTemplate slaveJdbcTemplate(
            @Qualifier("slaveDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean(name = "slaveEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean
    entityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("slaveDataSource") DataSource dataSource
    ) {
        return builder
                .dataSource(dataSource)
                .packages("com.example.slave") // 【3】这里是实体类的包路径
                .persistenceUnit("slave") // 这里写成唯一的就可以了，具体我也不太明白 ，希望有人告知
                // 【4】
                .properties(jpaProperties.getHibernateProperties(new HibernateSettings()))
                .build();
    }

    @Bean(name = "slaveTransactionManager")
    public PlatformTransactionManager transactionManager(
            @Qualifier("slaveEntityManagerFactory") EntityManagerFactory
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
    @Bean(name = "slaveSqlSessionFactory")
    @Primary
    public SqlSessionFactory masterSqlSessionFactory(@Qualifier("slaveDataSource") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(dataSource);
        bean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath:mapper/slave/**/*.xml"));
        return bean.getObject();
    }

    /**
     * 创建事务
     *
     * @param dataSource
     * @return DataSourceTransactionManager
     */
    @Bean(name = "slaveTransactionManager")
    @Primary
    public DataSourceTransactionManager masterDataSourceTransactionManager(@Qualifier("slaveDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    /**
     * 创建模板
     *
     * @param sqlSessionFactory
     * @return SqlSessionTemplate
     */
    @Bean(name = "slaveSqlSessionTemplate")
    @Primary
    public SqlSessionTemplate masterSqlSessionTemplate(@Qualifier("slaveSqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }
}
