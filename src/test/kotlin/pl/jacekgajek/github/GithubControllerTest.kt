package pl.jacekgajek.github

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

private const val USER_NAME = "jacekgajek"

@SpringBootTest
class GithubControllerTest {

    @Autowired
    private lateinit var githubClient: GithubClient

    @Autowired
    private lateinit var mvc: MockMvc

    @Test
    fun repos404() {
        mvc.get("/api/v1/repos/{username}", USER_NAME)
            .andExpect {
                status { isOk() }
                content {
                    this.contentType(MediaType.APPLICATION_JSON)
                    this.json("""
                        """)
                }
            }

    }
}