package com.unifor.tarefas_spring;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:tarefas;MODE=MySQL")
class TarefasSpringApplicationTests {

	@Test
	void contextLoads() {
	}

}
