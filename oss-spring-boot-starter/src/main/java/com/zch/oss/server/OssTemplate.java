package com.zch.oss.server;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.HttpMethod;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import com.zch.oss.OssProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.InitializingBean;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import java.time.Instant;
import java.util.Optional;

/**
 * @author Poison02
 * @date 2024/5/18
 */
@RequiredArgsConstructor
public class OssTemplate implements InitializingBean {

	private final OssProperties ossProperties;

	private AmazonS3 amazonS3;

	/**
	 * 创建存储桶
	 * @param bucketName
	 */
	public void createBucket(String bucketName) {
		if (! amazonS3.doesBucketExistV2(bucketName)) {
			amazonS3.createBucket(bucketName);
		}
	}

	/**
	 * 获取全部存储桶
	 * @return
	 */
	public List<Bucket> getAllBuckets() {
		return amazonS3.listBuckets();
	}

	/**
	 * 返回指定存储桶
	 * @param bucketName
	 * @return
	 */
	public Optional<Bucket> getBucket(String bucketName) {
		return amazonS3.listBuckets().stream().filter(b -> b.getName().equals(bucketName)).findFirst();
	}

	/**
	 * 删除存储桶
	 * @param bucketName
	 */
	public void removeBucket(String bucketName) {
		amazonS3.deleteBucket(bucketName);
	}

	/**
	 * 根据存储桶和文件前缀查找文件
	 * @param bucketName
	 * @param prefix
	 * @return
	 */
	public List<S3ObjectSummary> getAllObjectsByPrefix(String bucketName, String prefix) {
		ObjectListing objectListing = amazonS3.listObjects(bucketName, prefix);
		return new ArrayList<>(objectListing.getObjectSummaries());
	}

	/**
	 * 获取文件外链，用于下载文件
	 * @param bucketName
	 * @param objectName
	 * @param minutes
	 * @return
	 */
	public String getObjectURL(String bucketName, String objectName, int minutes) {
		return getObjectURL(bucketName, objectName, Duration.ofMinutes(minutes));
	}

	/**
	 * 获取文件外链，用于下载文件
	 * @param bucketName
	 * @param objectName
	 * @param expires
	 * @return
	 */
	public String getObjectURL(String bucketName, String objectName, Duration expires) {
		return getObjectURL(bucketName, objectName, expires, HttpMethod.GET);
	}

	/**
	 * 获取文件外链，用于上传文件
	 * @param bucketName
	 * @param objectName
	 * @param minutes
	 * @return
	 */
	public String getPutObjectURL(String bucketName, String objectName, int minutes) {
		return getPutObjectURL(bucketName, objectName, Duration.ofMinutes(minutes));
	}

	/**
	 * 获取文件外链，用于上传文件
	 * @param bucketName
	 * @param objectName
	 * @param expires
	 * @return
	 */
	public String getPutObjectURL(String bucketName, String objectName, Duration expires) {
		return getObjectURL(bucketName, objectName, expires, HttpMethod.PUT);
	}

	/**
	 * 获取文件外链
	 * @param bucketName
	 * @param objectName
	 * @param minutes
	 * @param method
	 * @return
	 */
	public String getObjectURL(String bucketName, String objectName, int minutes, HttpMethod method) {
		return getObjectURL(bucketName, objectName, Duration.ofMinutes(minutes), method);
	}

	/**
	 * 获取文件外链
	 * @param bucketName
	 * @param objectName
	 * @param expires
	 * @param method
	 * @return
	 */
	public String getObjectURL(String bucketName, String objectName, Duration expires, HttpMethod method) {
		// Set the pre-signed URL to expire after `expires`.
		Date expiration = Date.from(Instant.now().plus(expires));

		// Generate the pre-signed URL.
		URL url = amazonS3.generatePresignedUrl(
			new GeneratePresignedUrlRequest(bucketName, objectName).withMethod(method).withExpiration(expiration));
		return url.toString();
	}

	/**
	 * 获取文件URL
	 * @param bucketName
	 * @param objectName
	 * @return
	 */
	public String getObjectURL(String bucketName, String objectName) {
		URL url = amazonS3.getUrl(bucketName, objectName);
		return url.toString();
	}

	/**
	 * 获取文件
	 * @param bucketName
	 * @param objectName
	 * @return
	 */
	public S3Object getObject(String bucketName, String objectName) {
		return amazonS3.getObject(bucketName, objectName);
	}

	/**
	 * 上传文件
	 * @param bucketName
	 * @param objectName
	 * @param stream
	 * @throws IOException
	 */
	public void putObject(String bucketName, String objectName, InputStream stream) throws IOException {
		putObject(bucketName, objectName, stream, stream.available(), "application/octet-stream");
	}

	/**
	 * 上传文件 指定 content-type
	 * @param bucketName
	 * @param objectName
	 * @param contextType
	 * @param stream
	 * @throws IOException
	 */
	public void putObject(String bucketName, String objectName, String contextType, InputStream stream)
		throws IOException {
		putObject(bucketName, objectName, stream, stream.available(), contextType);
	}

	/**
	 * 上传文件
	 * @param bucketName
	 * @param objectName
	 * @param stream
	 * @param size
	 * @param contextType
	 * @return
	 */
	public PutObjectResult putObject(String bucketName, String objectName, InputStream stream, long size,
									 String contextType) {
		ObjectMetadata objectMetadata = new ObjectMetadata();
		objectMetadata.setContentLength(size);
		objectMetadata.setContentType(contextType);
		PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, objectName, stream, objectMetadata);
		// Setting the read limit value to one byte greater than the size of stream will
		// reliably avoid a ResetException
		putObjectRequest.getRequestClientOptions().setReadLimit(Long.valueOf(size).intValue() + 1);
		return amazonS3.putObject(putObjectRequest);

	}

	/**
	 * 获取文件信息
	 * @param bucketName
	 * @param objectName
	 * @return
	 */
	public S3Object getObjectInfo(String bucketName, String objectName) {
		return amazonS3.getObject(bucketName, objectName);
	}

	/**
	 * 删除文件
	 * @param bucketName
	 * @param objectName
	 */
	public void removeObject(String bucketName, String objectName) {
		amazonS3.deleteObject(bucketName, objectName);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		ClientConfiguration clientConfiguration = new ClientConfiguration();
		AwsClientBuilder.EndpointConfiguration endpointConfiguration = new AwsClientBuilder.EndpointConfiguration(
			ossProperties.getEndpoint(), ossProperties.getRegion());
		AWSCredentials awsCredentials = new BasicAWSCredentials(ossProperties.getAccessKey(),
			ossProperties.getSecretKey());
		AWSCredentialsProvider awsCredentialsProvider = new AWSStaticCredentialsProvider(awsCredentials);
		this.amazonS3 = AmazonS3Client.builder().withEndpointConfiguration(endpointConfiguration)
			.withClientConfiguration(clientConfiguration).withCredentials(awsCredentialsProvider)
			.disableChunkedEncoding().withPathStyleAccessEnabled(ossProperties.getPathStyleAccess()).build();
	}
}
