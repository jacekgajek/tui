package pl.jacekgajek.github

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import org.junit.jupiter.api.BeforeEach
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.autoconfigure.webservices.client.AutoConfigureMockWebServiceServer
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.reactive.server.WebTestClient
import java.time.Duration

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWireMock(port = 0)
@AutoConfigureWebTestClient(timeout = "PT1H")
@AutoConfigureMockWebServiceServer
@TestPropertySource(properties = ["jacekgajek.github-api-url=http://localhost:\${wiremock.server.port}"])
abstract class AbstractIntegrationTest {
    private val log = LoggerFactory.getLogger(AbstractIntegrationTest::class.java)

    @Autowired
    protected lateinit var objectMapper: ObjectMapper

    @Autowired
    protected lateinit var web: WebTestClient

    @Autowired
    protected lateinit var server: WireMockServer

    @BeforeEach
    fun setup() {
        web = web.mutate()
            .responseTimeout(Duration.ofDays(1))
            .build()
        server.resetAll()
        server.addMockServiceRequestListener { request, response ->
            log.info("WireMock request: {}\n{}\n{}", request.absoluteUrl, request.headers, request.body);
            log.info("WireMock response: {}\n{}\n{}", response.status, response.headers, response.bodyAsString);
        }
    }
}
