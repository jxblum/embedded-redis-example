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

import java.io.IOException;
import java.util.Optional;

import io.codeprimate.examples.redis.embedded.connection.EmbeddedRedisServerConnectionFactory;
import io.codeprimate.examples.redis.embedded.support.AbstractServerSupport;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.SmartLifecycle;

import redis.embedded.RedisExecProvider;
import redis.embedded.RedisServer;
import redis.embedded.util.OsArchitecture;

/**
 * Spring {@link FactoryBean} for the embedded {@link RedisServer}.
 *
 * @author John Blum
 * @see io.codeprimate.examples.redis.embedded.connection.EmbeddedRedisServerConnectionFactory
 * @see io.codeprimate.examples.redis.embedded.support.AbstractServerSupport
 * @see org.springframework.beans.factory.FactoryBean
 * @see org.springframework.context.SmartLifecycle
 * @see redis.embedded.RedisServer
 * @since 0.1.0
 */
@SuppressWarnings("unused")
public class EmbeddedRedisServerFactoryBean extends AbstractServerSupport
		implements FactoryBean<RedisServer>, SmartLifecycle {

	private static final int REDIS_PORT = EmbeddedRedisServerConnectionFactory.DEFAULT_REDIS_PORT;

	private static final String REDIS_READY_PATTERN = ".*Ready to accept connections tcp.*";

	private final RedisServer redisServer;

	public EmbeddedRedisServerFactoryBean(EmbeddedRedisServerProperties properties) {
		int resolvedPort = resolvePort(properties.port());
		this.redisServer = newRedisServer(properties, resolvedPort);
	}

	private RedisServer buildRedisServer(EmbeddedRedisServerProperties properties, int port) {

		return RedisServer.builder()
			.redisExecProvider(newRedisExecProvider(properties))
			.port(port)
			.build();
	}

	private RedisServer newRedisServer(EmbeddedRedisServerProperties properties, int port) {

		try {
			return new RedisServer(newRedisExecProvider(properties), port) {

				@Override
				protected String redisReadyPattern() {
					return REDIS_READY_PATTERN;
				}
			};
		}
		catch (IOException e) {
			throw new RuntimeException("Failed to start Redis server on port [%d]".formatted(port), e);
		}
	}

	private RedisExecProvider newRedisExecProvider(EmbeddedRedisServerProperties properties) {
		RedisExecProvider redisExecProvider = RedisExecProvider.defaultProvider();
		properties.optionalExec().ifPresent(executable -> {
			OsArchitecture osArchitecture = resolveOsArchitecture();
			redisExecProvider.override(osArchitecture.os(), osArchitecture.arch(), executable.getAbsolutePath());
		});
		return redisExecProvider;
	}

	private OsArchitecture resolveOsArchitecture() {
		return OsArchitecture.detect();
	}

	@Override
	public RedisServer getObject() {
		return requireRedisServer();
	}

	@Override
	public Class<?> getObjectType() {

		return getOptionalRedisServer()
			.<Class<?>>map(Object::getClass)
			.orElse(RedisServer.class);
	}

	@Override
	public boolean isRunning() {

		return getOptionalRedisServer()
			.filter(RedisServer::isActive)
			.isPresent();
	}

	@Override
	public void start() {
		requireRedisServer().start();
	}

	@Override
	public void stop() {
		getOptionalRedisServer().ifPresent(RedisServer::stop);
	}

	@Override
	protected int getDefaultServerPort() {
		return REDIS_PORT;
	}

	protected RedisServer getRedisServer() {
		return this.redisServer;
	}

	protected Optional<RedisServer> getOptionalRedisServer() {
		return Optional.ofNullable(getRedisServer());
	}

	protected RedisServer requireRedisServer() {
		return getOptionalRedisServer().orElseThrow(() -> new IllegalStateException("RedisServer not initialized"));
	}
}
