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
package io.codeprimate.examples.redis.embedded.connection;

import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisClusterConnection;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisSentinelConnection;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.util.Assert;

import redis.embedded.RedisServer;

/**
 * Spring Data Redis {@link RedisConnectionFactory} implementation for embedded {@link RedisServer}.
 * <p/>
 * Uses the Lettuce Redis Client Driver and {@link LettuceConnectionFactory} by default.
 * Override {@link #newRedisConnectionFactory(RedisServer)} to use a different Redis Client Driver. Becareful not to
 * let the {@literal this} reference to escape in a multithreaded application environment!
 *
 * @author John Blum
 * @see org.springframework.data.redis.connection.RedisConnectionFactory
 * @see org.springframework.data.redis.connection.RedisConnection
 * @see redis.embedded.RedisServer
 * @since 0.1.0
 */
@SuppressWarnings("all")
public class EmbeddedRedisServerConnectionFactory implements RedisConnectionFactory {

	public static final int DEFAULT_REDIS_PORT = 6379;

	public static final String LOCALHOST = "localhost";
	public static final String EMBEDDED_REDIS_HOST = LOCALHOST;

	private static RedisServer assertRedisServer(RedisServer redisServer) {
		Assert.notNull(redisServer, "Embedded RedisServer is required");
		return redisServer;
	}

	public static EmbeddedRedisServerConnectionFactory from(RedisServer redisServer) {
		return new EmbeddedRedisServerConnectionFactory(redisServer);
	}

	private final RedisConnectionFactory redisConnectionFactory;

	private final RedisServer redisServer;

	public EmbeddedRedisServerConnectionFactory(RedisServer redisServer) {
		this.redisServer = assertRedisServer(redisServer);
		this.redisConnectionFactory = newRedisConnectionFactory(redisServer);
	}

	// When overridding, becareful not to let the 'this' reference escape!
	protected RedisConnectionFactory newRedisConnectionFactory(RedisServer redisServer) {
		String host = resolveHost(redisServer);
		int port = resolvePort(redisServer);
		return new LettuceConnectionFactory(host, port);
	}

	// When overridding, becareful not to let the 'this' reference escape!
	protected String resolveHost(RedisServer redisServer) {
		return EMBEDDED_REDIS_HOST;
	}

	// When overridding, becareful not to let the 'this' reference escape!
	protected int resolvePort(RedisServer redisServer) {
		return getFirst(assertRedisServer(redisServer).ports());
	}

	private int getFirst(List<Integer> ports) {
		return isNotEmpty(ports) ? ports.get(0) : DEFAULT_REDIS_PORT;
	}

	private boolean isNotEmpty(List<?> list) {
		return !(list == null || list.isEmpty());
	}

	protected RedisConnectionFactory getRedisConnectionFactory() {
		return this.redisConnectionFactory;
	}

	protected RedisServer getRedisServer() {
		return this.redisServer;
	}

	@Override
	public boolean getConvertPipelineAndTxResults() {
		return getRedisConnectionFactory().getConvertPipelineAndTxResults();
	}

	@Override
	public RedisConnection getConnection() {
		return getRedisConnectionFactory().getConnection();
	}

	@Override
	public RedisClusterConnection getClusterConnection() {
		return getRedisConnectionFactory().getClusterConnection();
	}

	@Override
	public RedisSentinelConnection getSentinelConnection() {
		return getRedisConnectionFactory().getSentinelConnection();
	}

	@Override
	public DataAccessException translateExceptionIfPossible(RuntimeException e) {
		return getRedisConnectionFactory().translateExceptionIfPossible(e);
	}
}
