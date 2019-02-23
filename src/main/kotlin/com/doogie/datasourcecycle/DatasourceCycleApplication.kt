package com.doogie.datasourcecycle

import com.zaxxer.hikari.HikariDataSource
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter
import org.springframework.transaction.PlatformTransactionManager
import java.util.Properties
import javax.sql.DataSource

@SpringBootApplication
@EnableJpaRepositories(
    transactionManagerRef = "transactionManager",
    entityManagerFactoryRef = "entityManagerFactory"
)
class DatasourceCycleApplication {
    @Bean
    fun transactionManager(): PlatformTransactionManager {
        return JpaTransactionManager(entityManagerFactory().`object`!!)
    }

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
        val factory = LocalContainerEntityManagerFactoryBean()
        factory.jpaVendorAdapter = HibernateJpaVendorAdapter()
        factory.dataSource = dataSource()
        factory.setPackagesToScan("com.doogie.datasourcecycle")
        return factory
    }
}

fun main(args: Array<String>) {
    runApplication<DatasourceCycleApplication>(*args) {
       this.setDefaultProperties(Properties().also {
           it["management.server.port"] = 18080
       })
    }
}
