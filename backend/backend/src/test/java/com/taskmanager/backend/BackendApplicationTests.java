// src/test/java/com/taskmanager/backend/BackendApplicationTests.java

package com.taskmanager.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
// ↑ @ActiveProfiles("test") = "Benutze die test-Konfiguration"
//   Spring sucht dann: application-test.yml ODER
//   src/test/resources/application.yml
class BackendApplicationTests {

	@Test
	void contextLoads() {
		// Dieser Test prüft nur ob Spring Boot fehlerfrei startet.
		// Wenn die Konfiguration falsch ist → Test schlägt fehl.
		// Kein Code nötig – das Starten selbst ist der Test.
	}
}