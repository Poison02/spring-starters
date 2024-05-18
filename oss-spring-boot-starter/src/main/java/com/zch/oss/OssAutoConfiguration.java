package com.zch.oss;

import com.zch.oss.http.OssEndpoint;
import com.zch.oss.server.OssTemplate;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Poison02
 * @date 2024/5/18
 */
@AutoConfiguration
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties({ OssProperties.class })
public class OssAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean(OssTemplate.class)
	@ConditionalOnProperty(prefix = OssProperties.PREFIX, name = "enable", havingValue = "true", matchIfMissing = true)
	public OssTemplate ossTemplate(OssProperties properties) {
		return new OssTemplate(properties);
	}

	@Bean
	@ConditionalOnWebApplication
	@ConditionalOnProperty(prefix = OssProperties.PREFIX, name = "http.enable", havingValue = "true")
	public OssEndpoint ossEndpoint(OssTemplate template) {
		return new OssEndpoint(template);
	}

}
