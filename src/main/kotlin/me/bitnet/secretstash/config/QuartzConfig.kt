package me.bitnet.secretstash.config

import org.springframework.boot.autoconfigure.quartz.SchedulerFactoryBeanCustomizer
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.quartz.SchedulerFactoryBean

@Configuration
class QuartzConfig : SchedulerFactoryBeanCustomizer {
    override fun customize(schedulerFactoryBean: SchedulerFactoryBean) {
        schedulerFactoryBean.setOverwriteExistingJobs(true)
        schedulerFactoryBean.setWaitForJobsToCompleteOnShutdown(true)
    }
}
