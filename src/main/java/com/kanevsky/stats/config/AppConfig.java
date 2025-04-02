package com.kanevsky.stats.config;

import com.kanevsky.stats.repos.IStatsManager;
import com.kanevsky.stats.repos.StatsManager;
import com.kanevsky.stats.repos.StatsRepo;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class AppConfig implements WebMvcConfigurer {

    @Bean
    public StatsRepo playerStatsRepo() {
        return new StatsRepo();
    }

    @Bean
    public StatsRepo teamStatsRepo() {
        return new StatsRepo();
    }

    @Bean(name = "playerStatsManager")
    public IStatsManager playerStatsManager() {
        return new StatsManager(playerStatsRepo());
    }

    @Bean(name = "teamStatsManager")
    public IStatsManager teamStatsManager() {
        return new StatsManager(teamStatsRepo());
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowedHeaders("*");
    }
}