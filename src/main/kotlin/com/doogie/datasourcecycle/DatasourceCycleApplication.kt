package com.doogie.datasourcecycle

import com.zaxxer.hikari.HikariDataSource
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter
import java.util.Properties
import javax.sql.DataSource

@SpringBootApplication
class DatasourceCycleApplication {
    @Bean
    @Primary
    fun dataSourceProperties(): DataSourceProperties {
        return DataSourceProperties()
    }

    @Bean
    fun slaveDataSource(): DataSource {
        return dataSourceProperties()
            .initializeDataSourceBuilder()
            .type(HikariDataSource::class.java)
            .build()
    }

    @Bean
    fun dataSource(): DataSource {
        return slaveDataSource()
    }

    @Bean
    fun entityManagerFactory(): LocalContainerEntityManagerFactoryBean {
        return LocalContainerEntityManagerFactoryBean().apply {
            jpaVendorAdapter = HibernateJpaVendorAdapter()
            dataSource = dataSource()
            setPackagesToScan("com.doogie.datasourcecycle")
        }
    }
}

fun main(args: Array<String>) {
    runApplication<DatasourceCycleApplication>(*args) {
       this.setDefaultProperties(Properties().also {
           it["management.server.port"] = 18080
       })
    }
}
