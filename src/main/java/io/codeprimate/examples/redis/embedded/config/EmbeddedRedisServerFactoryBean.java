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

import java.util.Optional;

import io.codeprimate.examples.redis.embedded.connection.EmbeddedRedisServerConnectionFactory;
import io.codeprimate.examples.redis.embedded.support.AbstractServerSupport;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.SmartLifecycle;

import redis.embedded.RedisServer;

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
public class EmbeddedRedisServerFactoryBean extends AbstractServerSupport
		implements FactoryBean<RedisServer>, SmartLifecycle {

	private static final int REDIS_PORT = EmbeddedRedisServerConnectionFactory.DEFAULT_REDIS_PORT;

	private final RedisServer redisServer;

	public EmbeddedRedisServerFactoryBean(int port) {

		int resolvedPort = resolvePort(port);

		this.redisServer = RedisServer.builder()
			.port(resolvedPort)
			.build();
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
