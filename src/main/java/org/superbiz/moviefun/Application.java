package org.superbiz.moviefun;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class
})
public class Application {

    public static void main(String... args) {
        SpringApplication.run(Application.class, args);
    }

    @Autowired
    private Environment env;

    @Bean
    public ServletRegistrationBean actionServletRegistration(ActionServlet actionServlet) {
        return new ServletRegistrationBean(actionServlet, "/moviefun/*");
    }

    @Bean
    public DatabaseServiceCredentials serviceCredentials(){
        DatabaseServiceCredentials serviceCredentials = new DatabaseServiceCredentials(env.getProperty("VCAP_SERVICES"));
        return serviceCredentials;
    }
    @Bean
    public DataSource moviesDataSource(DatabaseServiceCredentials serviceCredentials) {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setDriverClassName("com.mysql.jdbc.Driver");
        hikariConfig.setJdbcUrl(serviceCredentials.jdbcUrl("movies-mysql", "p-mysql"));

        hikariConfig.setMaximumPoolSize(5);
        hikariConfig.setConnectionTestQuery("SELECT 1");
        hikariConfig.setPoolName("moviesHikariCP");

        hikariConfig.addDataSourceProperty("dataSource.cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("dataSource.prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("dataSource.prepStmtCacheSqlLimit", "2048");
        hikariConfig.addDataSourceProperty("dataSource.useServerPrepStmts", "true");

        HikariDataSource dataSource = new HikariDataSource(hikariConfig);

        return dataSource;


    }
    @Bean
    public DataSource albumsDataSource(DatabaseServiceCredentials serviceCredentials) {
        /*MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setURL(serviceCredentials.jdbcUrl("albums-mysql", "p-mysql"));
        return dataSource;*/

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setDriverClassName("com.mysql.jdbc.Driver");
        hikariConfig.setJdbcUrl(serviceCredentials.jdbcUrl("albums-mysql", "p-mysql"));


        hikariConfig.setMaximumPoolSize(5);
        hikariConfig.setConnectionTestQuery("SELECT 1");
        hikariConfig.setPoolName("albumsHikariCP");

        hikariConfig.addDataSourceProperty("dataSource.cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("dataSource.prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("dataSource.prepStmtCacheSqlLimit", "2048");
        hikariConfig.addDataSourceProperty("dataSource.useServerPrepStmts", "true");

        HikariDataSource dataSource = new HikariDataSource(hikariConfig);

        return dataSource;

    }
    @Bean
    public HibernateJpaVendorAdapter hibernateJpaVendorAdapter (){
        HibernateJpaVendorAdapter hibernateJpaVendorAdapter= new HibernateJpaVendorAdapter();
        hibernateJpaVendorAdapter.setDatabase(Database.MYSQL);
        hibernateJpaVendorAdapter.setDatabasePlatform("org.hibernate.dialect.MySQL5Dialect");
        hibernateJpaVendorAdapter.setGenerateDdl(true);
        return hibernateJpaVendorAdapter;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean localMoviesContainerEntityManagerFactoryBean(){
        LocalContainerEntityManagerFactoryBean localMoviesContainerEntityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
        localMoviesContainerEntityManagerFactoryBean.setDataSource(moviesDataSource(serviceCredentials()));
        localMoviesContainerEntityManagerFactoryBean.setJpaVendorAdapter(hibernateJpaVendorAdapter());
        localMoviesContainerEntityManagerFactoryBean.setPackagesToScan("org.superbiz.moviefun.movies");
        localMoviesContainerEntityManagerFactoryBean.setPersistenceUnitName("moviesDataSource");

        return localMoviesContainerEntityManagerFactoryBean;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean localAlbumsContainerEntityManagerFactoryBean(){
        LocalContainerEntityManagerFactoryBean localAlbumsContainerEntityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
        localAlbumsContainerEntityManagerFactoryBean.setDataSource(albumsDataSource(serviceCredentials()));
        localAlbumsContainerEntityManagerFactoryBean.setJpaVendorAdapter(hibernateJpaVendorAdapter());
        localAlbumsContainerEntityManagerFactoryBean.setPackagesToScan("org.superbiz.moviefun.albums");
        localAlbumsContainerEntityManagerFactoryBean.setPersistenceUnitName("albumsDataSource");

        return localAlbumsContainerEntityManagerFactoryBean;
    }

    @Bean
    public JpaTransactionManager moviesPlatformTransactionManager(){

        JpaTransactionManager jpaTransactionManager = new JpaTransactionManager();
        jpaTransactionManager.setEntityManagerFactory(localMoviesContainerEntityManagerFactoryBean().getObject());
        return jpaTransactionManager;
    }
    @Bean
    public JpaTransactionManager albumsPlatformTransactionManager(){

        JpaTransactionManager jpaTransactionManager = new JpaTransactionManager();
        jpaTransactionManager.setEntityManagerFactory(localAlbumsContainerEntityManagerFactoryBean().getObject());
        return jpaTransactionManager;
    }
}
