/*
 *  Copyright 2024 Author or Authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package io.codeprimate.examples.redis.embedded.config;

import io.codeprimate.examples.redis.embedded.config.support.AbstractImportAwareSupport;
import io.codeprimate.examples.redis.embedded.connection.EmbeddedRedisServerConnectionFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.NonNull;

import redis.embedded.RedisServer;

/**
 * Spring {@link Configuration} used to configure the embedded {@link RedisServer}.
 *
 * @author John Blum
 * @see redis.embedded.RedisServer
 * @see org.springframework.context.annotation.Configuration
 * @see io.codeprimate.examples.redis.embedded.config.EnableEmbeddedRedisServer
 * @since 0.1.0
 */
@Configuration
@EnableConfigurationProperties(EmbeddedRedisServerProperties.class)
@SuppressWarnings("unused")
public class EmbeddedRedisServerConfiguration extends AbstractImportAwareSupport<EnableEmbeddedRedisServer> {

	protected static final int REDIS_PORT = EmbeddedRedisServerConnectionFactory.DEFAULT_REDIS_PORT;

	private int redisPort = REDIS_PORT;

	@Autowired
	private EmbeddedRedisServerProperties embeddedRedisProperties;

	@Autowired
	private RedisProperties redisProperties;

	@Override
	public void setImportMetadata(@NonNull AnnotationMetadata importMetadata) {

		if (isAnnotationPresent(importMetadata)) {
			AnnotationAttributes enableEmbeddedRedisServerAttributes = getAnnotationAttributes(importMetadata);
			this.redisPort = getRedisPort(enableEmbeddedRedisServerAttributes);
		}
	}

	protected int getRedisPort(AnnotationAttributes enableEmbeddedRedisServerAttributes) {

		return this.redisProperties.getPort() != REDIS_PORT ? this.redisProperties.getPort()
			: this.embeddedRedisProperties.port() != null ? this.embeddedRedisProperties.port()
			: enableEmbeddedRedisServerAttributes.getNumber("port");
	}

	@Bean
	EmbeddedRedisServerFactoryBean embeddedRedisServer() {

		EmbeddedRedisServerProperties properties = EmbeddedRedisServerProperties.copy(this.embeddedRedisProperties)
			.usingPort(this.redisPort)
			.build();

		return new EmbeddedRedisServerFactoryBean(properties);
	}

	@Bean
	RedisTemplate<String, Object> embeddedRedisTemplate(RedisServer redisServer) {
		RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
		redisTemplate.setConnectionFactory(new EmbeddedRedisServerConnectionFactory(redisServer));
		return redisTemplate;
	}
}
