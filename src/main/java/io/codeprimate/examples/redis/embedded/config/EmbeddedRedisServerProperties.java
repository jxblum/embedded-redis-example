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

import java.io.File;
import java.util.Optional;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.Assert;

import lombok.AccessLevel;
import lombok.Getter;
import redis.embedded.RedisServer;

/**
 * Spring {@link EmbeddedRedisServerProperties} containing configuration metadata for the embedded {@link RedisServer}.
 *
 * @author John Blum
 * @see redis.embedded.RedisServer
 * @see org.springframework.boot.context.properties.ConfigurationProperties
 * @since 0.1.0
 */
@ConfigurationProperties("redis.server")
@SuppressWarnings("unused")
public record EmbeddedRedisServerProperties(File exec, Integer port) {

	public static int REDIS_PORT = EmbeddedRedisServerConfiguration.REDIS_PORT;

	public static EmbeddedRedisServerProperties.Builder copy(EmbeddedRedisServerProperties properties) {
		Assert.notNull(properties, "Properties to copy is required");
		return new Builder(properties);
	}

	public EmbeddedRedisServerProperties {
		boolean isValidExec = exec == null || exec.isFile();
		Assert.isTrue(isValidExec, () -> "Executable [%s] for Redis server not found".formatted(exec));
	}

	public Optional<File> optionalExec() {
		return Optional.ofNullable(exec());
	}

	public Optional<Integer> optionalPort() {
		return Optional.ofNullable(port());
	}

	public int portOrDefault() {
		return optionalPort().orElse(REDIS_PORT);
	}

	@Getter(AccessLevel.PROTECTED)
	public static class Builder {

		private File executable;
		private Integer port;

		protected Builder(EmbeddedRedisServerProperties properties) {
			this.executable = properties.exec();
			this.port = properties.port();
		}

		public Builder usingExecutable(File executable) {
			this.executable = executable;
			return this;
		}

		public Builder usingPort(Integer port) {
			this.port = port;
			return this;
		}

		public EmbeddedRedisServerProperties build() {
			return new EmbeddedRedisServerProperties(getExecutable(), getPort());
		}
	}
}
