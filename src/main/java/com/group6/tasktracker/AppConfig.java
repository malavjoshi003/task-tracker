package com.group6.tasktracker;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.time.Clock;
import java.time.ZoneId;
@Configuration
class AppConfig {
    @Bean Clock applicationClock() { return Clock.system(ZoneId.of("Asia/Kolkata")); }
}
