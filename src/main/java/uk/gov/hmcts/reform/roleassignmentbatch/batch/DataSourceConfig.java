
package uk.gov.hmcts.reform.roleassignmentbatch.batch;

import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
public class DataSourceConfig {

    @Value("${spring.datasource.url}")
    String url;

    @Value("${spring.datasource.username}")
    String userName;

    @Value("${spring.datasource.password}")
    String password;

    @Value("${spring.datasource.min-idle}")
    int idleConnections;

    @Value("${spring.datasource.max-life}")
    int maxLife;

    @Value("${spring.datasource.idle-timeout}")
    int idleTimeOut;

    @Value("${spring.datasource.maximum-pool-size}")
    int maxPoolSize;


    @Bean("springJdbcDataSource")
    public DataSource springJdbcDataSource() {
        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
        dataSourceBuilder.driverClassName("org.postgresql.Driver");
        dataSourceBuilder.url(url);
        dataSourceBuilder.username(userName);
        dataSourceBuilder.password(password);
        HikariDataSource dataSource = (HikariDataSource) dataSourceBuilder.build();
        dataSource.setMinimumIdle(idleConnections);
        dataSource.setIdleTimeout(idleTimeOut);
        //dataSource.setMaxLifetime(maxLife);
        dataSource.setMaximumPoolSize(maxPoolSize);
        return dataSourceBuilder.build();
    }


    @Bean("springJdbcTemplate")
    JdbcTemplate springJdbcTemplate() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate();
        jdbcTemplate.setDataSource(springJdbcDataSource());
        return jdbcTemplate;
    }


}
