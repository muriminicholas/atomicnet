package com.atomicnet;

import com.atomicnet.entity.PackageInfo;
import com.atomicnet.repository.PackageInfoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DatabaseSeeder {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseSeeder.class);

    @Bean
    CommandLineRunner initDatabase(PackageInfoRepository repository) {
        return args -> {
            logger.info("Checking package_info table for seeding...");
            // Check if specific packages exist, insert if missing
            String[] packageTypes = {
                "one_hour", "two_hour", "four_hour", "six_hour",
                "one_day", "two_day", "weekly", "monthly"
            };
            PackageInfo[] packages = {
                new PackageInfo("one_hour", 10, 1, 5),
                new PackageInfo("two_hour", 15, 2, 5),
                new PackageInfo("four_hour", 25, 4, 5),
                new PackageInfo("six_hour", 30, 6, 5),
                new PackageInfo("one_day", 40, 24, 5),
                new PackageInfo("two_day", 70, 48, 5),
                new PackageInfo("weekly", 250, 168, 5),
                new PackageInfo("monthly", 900, 720, 5)
            };

            for (int i = 0; i < packageTypes.length; i++) {
                if (!repository.findByType(packageTypes[i]).isPresent()) {
                    repository.save(packages[i]);
                    logger.info("Inserted package: type={}, price={}, duration={}, bandwidth={}",
                        packages[i].getType(), packages[i].getPrice(),
                        packages[i].getDurationHours(), packages[i].getBandwidthMbps());
                } else {
                    logger.info("Package {} already exists in database", packageTypes[i]);
                }
            }
            logger.info("Seeding completed. Total package_info entries: {}", repository.count());
        };
    }
}