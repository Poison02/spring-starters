package com.zch.oss;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Poison02
 * @date 2024/5/18
 */
@Data
@ConfigurationProperties(prefix = OssProperties.PREFIX)
public class OssProperties {

	/**
	 * 配置前缀
	 */
	public static final String PREFIX = "oss";

	/**
	 * 是否启用oss 默认true
	 */
	private boolean enable = true;

	/**
	 * 对象存储服务的URL
	 */
	private String endpoint;

	/**
	 * 自定义域名
	 */
	private String customDomain;

	/**
	 * true path-style nginx 反向代理和S3默认支持 pathStyle {http://endpoint/bucketname} false
	 * supports virtual-hosted-style 阿里云等需要配置为 virtual-hosted-style
	 * 模式{http://bucketname.endpoint}
	 */
	private Boolean pathStyleAccess = true;

	/**
	 * 地区
	 */
	private String region;

	/**
	 * access key
	 */
	private String accessKey;

	/**
	 * Secret key是你账户的密码
	 */
	private String secretKey;

	/**
	 * 默认的存储桶名称
	 */
	private String bucketName;

}
