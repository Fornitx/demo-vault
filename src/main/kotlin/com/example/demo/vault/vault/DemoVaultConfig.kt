package com.example.demo.vault.vault

import com.example.demo.vault.vault.providers.CachedCredentialsProvider
import com.example.demo.vault.vault.providers.CredentialsProvider
import com.example.demo.vault.vault.providers.UpdatableCredentialsProvider
import io.github.oshai.kotlinlogging.KotlinLogging
import io.r2dbc.spi.ConnectionFactoryOptions
import io.r2dbc.spi.Option
import org.quartz.Calendar
import org.quartz.JobDetail
import org.quartz.Trigger
import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty
import org.springframework.boot.quartz.autoconfigure.QuartzProperties
import org.springframework.boot.quartz.autoconfigure.SchedulerFactoryBeanCustomizer
import org.springframework.boot.r2dbc.autoconfigure.ConnectionFactoryOptionsBuilderCustomizer
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.quartz.SchedulerFactoryBean
import org.springframework.scheduling.quartz.SpringBeanJobFactory
import java.util.*
import java.util.function.Supplier

private val log = KotlinLogging.logger {}

@Configuration
@ConditionalOnBooleanProperty("vault.enabled")
class DemoVaultConfig {
    @Bean
    fun demoVaultClient(vaultProperties: DemoVaultProperties): DemoVaultClient {
        return DemoVaultClient(vaultProperties)
    }

    @Bean
    fun credentialsProvider(vaultClient: DemoVaultClient): CachedCredentialsProvider {
        return CachedCredentialsProvider { vaultClient.getCredentials() }
    }

    @Bean
    fun credentialsProviderConnectionFactoryOptionsBuilderCustomizer(
        credentialsProvider: CredentialsProvider,
    ): ConnectionFactoryOptionsBuilderCustomizer {
        return ConnectionFactoryOptionsBuilderCustomizer { builder ->
            builder.option(
                Option.valueOf(ConnectionFactoryOptions.USER.name()),
                Supplier { credentialsProvider.getCredentials().username },
            ).option(
                Option.sensitiveValueOf(ConnectionFactoryOptions.PASSWORD.name()),
                Supplier { credentialsProvider.getCredentials().password },
            )
        }
    }

    @Bean
    fun quartzScheduler(
        properties: QuartzProperties,
        customizers: ObjectProvider<SchedulerFactoryBeanCustomizer>,
        jobDetails: ObjectProvider<JobDetail>,
        calendars: MutableMap<String, Calendar>,
        triggers: ObjectProvider<Trigger>,
        applicationContext: ApplicationContext,
        credentialsProvider: CredentialsProvider,
    ): SchedulerFactoryBean {
        val schedulerFactoryBean = SchedulerFactoryBean()
        val jobFactory = SpringBeanJobFactory()
        jobFactory.setApplicationContext(applicationContext)
        schedulerFactoryBean.setJobFactory(jobFactory)
        if (properties.schedulerName != null) {
            schedulerFactoryBean.setSchedulerName(properties.schedulerName!!)
        }
        schedulerFactoryBean.isAutoStartup = properties.isAutoStartup
        schedulerFactoryBean.setStartupDelay(properties.startupDelay.seconds.toInt())
        schedulerFactoryBean.setWaitForJobsToCompleteOnShutdown(properties.isWaitForJobsToCompleteOnShutdown)
        schedulerFactoryBean.setOverwriteExistingJobs(properties.isOverwriteExistingJobs)
        if (properties.properties.isNotEmpty()) {
            val quartzProperties = asProperties(properties.properties)

            val credentials = credentialsProvider.getCredentials()
            quartzProperties["org.quartz.dataSource.quartzDataSource.user"] = credentials.username
            quartzProperties["org.quartz.dataSource.quartzDataSource.password"] = credentials.password

            schedulerFactoryBean.setQuartzProperties(quartzProperties)
        }
        schedulerFactoryBean.setJobDetails(*jobDetails.orderedStream().toArray { arrayOfNulls(it) })
        schedulerFactoryBean.setCalendars(calendars)
        schedulerFactoryBean.setTriggers(*triggers.orderedStream().toArray { arrayOfNulls(it) })
        customizers.orderedStream().forEach { it.customize(schedulerFactoryBean) }
        return schedulerFactoryBean
    }

    @Bean
    @ConditionalOnAvailableEndpoint
    @ConditionalOnBooleanProperty("management.endpoint.vault.enabled")
    fun vaultEndpoint(credentialsProvider: UpdatableCredentialsProvider): DemoVaultEndpoint {
        return DemoVaultEndpoint(credentialsProvider)
    }

    private fun asProperties(source: MutableMap<String, String>): Properties {
        val properties = Properties()
        properties.putAll(source)
        return properties
    }
}