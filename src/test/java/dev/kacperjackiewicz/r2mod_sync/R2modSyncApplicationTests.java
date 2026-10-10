package dev.kacperjackiewicz.r2mod_sync;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
		"app.security.email=admin",
		"app.security.password=admin"
})
class R2modSyncApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void loginPageIsPublic() throws Exception {
		mockMvc.perform(get("/login.html"))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith("text/html"))
				.andExpect(content().string(containsString("<h1>Sign in</h1>")));
	}

	@Test
	void publicHomeCanBeLoadedWithoutLogin() throws Exception {
		mockMvc.perform(get("/"))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith("text/html"))
				.andExpect(content().string(containsString("Welcome to r2mod-sync.")));
	}

	@Test
	void protectedIndexRedirectsToLogin() throws Exception {
		mockMvc.perform(get("/server.html"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("http://localhost/login.html"));
	}

	@Test
	void loginEndpointAcceptsConfiguredUsernameAndPassword() throws Exception {
		mockMvc.perform(post("/login")
						.with(csrf())
						.param("text", "admin")
						.param("password", "admin"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/server.html"));
	}

	@Test
	void authenticatedUserCanLoadIndexPage() throws Exception {
		mockMvc.perform(get("/server.html").with(user("admin")))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith("text/html"))
				.andExpect(content().string(containsString("<h1>r2mod-sync</h1>")))
				.andExpect(content().string(containsString("You are logged in.")));
	}

}
