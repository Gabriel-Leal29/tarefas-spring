package com.unifor.tarefas_spring;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// Nos testes, o MySQL é trocado por um H2 em memória (modo de compatibilidade com o MySQL)
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:tarefas;MODE=MySQL")
class TarefasSpringApplicationTests {

	@Test
	void contextLoads() {
	}

}
