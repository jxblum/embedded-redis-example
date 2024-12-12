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
package io.codeprimate.examples.redis.embedded;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@SpringBootTest
@SuppressWarnings("unused")
class EmbeddedRedisExampleApplicationTests {

	@Autowired
	private RedisTemplate<String, Object> embeddedRedisTemplate;

	@Test
	void embeddedRedisGetAndSetSuccessfully() {

		ValueOperations<String, Object> valueOperations = this.embeddedRedisTemplate.opsForValue();

		assertThat(valueOperations).isNotNull();
		assertThat(valueOperations.get("TestKey")).isNull();

		valueOperations.set("TestKey", "TestValue");

		assertThat(valueOperations.get("TestKey")).isEqualTo("TestValue");
	}

}
