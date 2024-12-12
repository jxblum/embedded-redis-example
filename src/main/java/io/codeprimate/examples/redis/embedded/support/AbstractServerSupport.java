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
package io.codeprimate.examples.redis.embedded.support;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.function.BiFunction;

import org.slf4j.Logger;
import org.springframework.util.Assert;

import lombok.extern.slf4j.Slf4j;

/**
 * Abstract base class encapsulating common functionality to create and run server-based applications.
 *
 * @author John Blum
 * @see java.net.ServerSocket
 * @since 0.1.0
 */
@Slf4j
@SuppressWarnings("unused")
public abstract class AbstractServerSupport {

	protected static final int EPHEMERAL_PORT = 0;

	private static final BiFunction<Integer, Integer, Integer> AVAILABLE_PORT_FUNCTION = (port, defaultPort) -> {

		try {
			try (ServerSocket serverSocket = new ServerSocket(port)) {
				serverSocket.setReuseAddress(true);
				return serverSocket.getLocalPort();
			}
		}
		catch (IOException e) {
			if (log.isWarnEnabled()) {
				log.warn("Port [{}] not available; defaulting to [{}]", port, defaultPort);
				if (log.isDebugEnabled()) {
					log.debug("I/O error: ", e);
				}
			}
			return defaultPort;
		}
	};

	protected int assertAvailablePort(int port) {
		Assert.state(isAvailablePort(port), () -> "Port [%d] is not available".formatted(port));
		return port;
	}

	protected boolean isAvailablePort(int port) {
		int negatedPort = negate(port);
		return AVAILABLE_PORT_FUNCTION.apply(port,  negatedPort) != negatedPort;
	}

	protected boolean isCustomPort(int port) {
		return isValidPort(port) && port != getDefaultServerPort();
	}

	protected boolean isValidPort(int port) {
		return port > 0;
	}

	protected int getAvailablePort() {
		return getAvailablePort(getDefaultServerPort());
	}

	protected int getAvailablePort(int defaultPort) {
		return AVAILABLE_PORT_FUNCTION.apply(EPHEMERAL_PORT, defaultPort);
	}

	protected abstract int getDefaultServerPort();

	protected Logger getLogger() {
		return log;
	}

	private int negate(int value) {
		return -1 * value;
	}

	protected int resolvePort(int port) {
		return isCustomPort(port) ? assertAvailablePort(port) : getAvailablePort();
	}
}
