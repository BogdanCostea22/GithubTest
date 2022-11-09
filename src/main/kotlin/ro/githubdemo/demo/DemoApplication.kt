package ro.githubdemo.demo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import ro.githubdemo.demo.config.AppProperties

@SpringBootApplication
@EnableConfigurationProperties(AppProperties::class)
class DemoApplication

fun main(args: Array<String>) {
	runApplication<DemoApplication>(*args)
}
