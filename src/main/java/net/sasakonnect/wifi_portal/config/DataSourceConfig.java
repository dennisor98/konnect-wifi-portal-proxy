package net.sasakonnect.wifi_portal.config;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

@Configuration
public class DataSourceConfig {
	@Bean
	@Primary
	@ConfigurationProperties("spring.datasource.write")
	 DataSource writeDataSource() {
		return DataSourceBuilder.create().build();
	}

	@Bean
	@ConfigurationProperties("spring.datasource.read")
	DataSource readDataSource() {
		return DataSourceBuilder.create().build();
	}

	@Bean
	DataSource routingDataSource(
			@Qualifier("writeDataSource") DataSource writeDataSource,
			@Qualifier("readDataSource") DataSource readDataSource) {

		Map<Object, Object> targetDataSources = new HashMap<>();
		targetDataSources.put("WRITE", writeDataSource);
		targetDataSources.put("READ", readDataSource);

		AbstractRoutingDataSource routingDataSource = new AbstractRoutingDataSource() {
			@Override
			protected Object determineCurrentLookupKey() {
				return DataSourceContextHolder.get();
			}
		};
		routingDataSource.setDefaultTargetDataSource(writeDataSource);
		routingDataSource.setTargetDataSources(targetDataSources);
		return routingDataSource;
	}

	@Bean
	DataSource dataSource(DataSource routingDataSource) {
		return new LazyConnectionDataSourceProxy(routingDataSource);
	}
}
