package ro.githubdemo.demo.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConfigurationProperties(prefix = "tra")
@ConstructorBinding
data class AppProperties(
    val githubUrl: String,
    val githubToken: String,
)