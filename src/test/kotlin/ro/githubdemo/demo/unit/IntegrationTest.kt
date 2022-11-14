package ro.githubdemo.demo.unit

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.eclipse.jetty.http.HttpHeader
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import ro.githubdemo.demo.config.AppProperties
import ro.githubdemo.demo.unit.fixture.RepositoryFixture

@SpringBootTest
@AutoConfigureWebTestClient
class IntegrationTest {
    companion object {
        const val GITHUB_TOKEN = "GITHUB_TOKEN"
    }

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @MockBean
    lateinit var properties: AppProperties

    lateinit var mockBackEnd: MockWebServer

    @BeforeEach
    fun setUp() {
        mockBackEnd = MockWebServer()
        mockBackEnd.start()
    }

    @AfterEach
    fun tearDown() {
        mockBackEnd.shutdown()

        verifyNoMoreInteractions(properties)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun `Given username and headers when fetching user repositories return all of them with the last commit`() =
        runTest {
            val serverUrl = mockBackEnd.url("/").toString()

            doReturn(serverUrl).whenever(properties).githubUrl
            doReturn(GITHUB_TOKEN).whenever(properties).githubToken

            val repositoryResponse = RepositoryFixture.createRepositoryResponse()
            val repositoryList = listOf(repositoryResponse)
            val requestResponse = ObjectMapper().writeValueAsString(repositoryList)

            val repositoryBranch = RepositoryFixture.createRepositoryBranchesResponse()
            val branches = listOf(repositoryBranch)
            val branchesResponse = ObjectMapper().writeValueAsString(branches)

            val backendResponse = MockResponse()
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .setBody(requestResponse)

            val mockServerBranchesResponse = MockResponse()
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .setBody(branchesResponse)

            mockBackEnd.enqueue(backendResponse)
            mockBackEnd.enqueue(mockServerBranchesResponse)

            //Act & Assert
            val response = webTestClient.get()
                .uri { uriBuilder ->
                    uriBuilder.path("/repositories")
                        .queryParam("username", "username")
                        .build()
                }
                .header(HttpHeader.ACCEPT.name, MediaType.APPLICATION_JSON_VALUE)
                .exchange()

            response.expectStatus().isOk

            verify(properties, times(2)).githubUrl
            verify(properties, times(2)).githubToken
        }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun `Given username and header when header not match then return an appropriate error`() =
        runTest {
            val serverUrl = mockBackEnd.url("/").toString()

            doReturn(serverUrl).whenever(properties).githubUrl
            doReturn(GITHUB_TOKEN).whenever(properties).githubToken

            //Act & Assert
            val response = webTestClient.get()
                .uri { uriBuilder ->
                    uriBuilder.path("/repositories")
                        .queryParam("username", "username")
                        .build()
                }
                .header(HttpHeader.ACCEPT.name, MediaType.APPLICATION_CBOR_VALUE)
                .exchange()

            response.expectStatus().isEqualTo(HttpStatus.NOT_ACCEPTABLE)

            verify(properties, never()).githubUrl
            verify(properties, never()).githubToken
        }


    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun `Given username and header when username doesn't exist then return 404 status code in response`() =
        runTest {
            val serverUrl = mockBackEnd.url("/").toString()

            doReturn(serverUrl).whenever(properties).githubUrl
            doReturn(GITHUB_TOKEN).whenever(properties).githubToken

            val backendResponse = MockResponse()
                .setResponseCode(HttpStatus.NOT_FOUND.value())
                .addHeader("Content-Type", "application/json; charset=utf-8")

            mockBackEnd.enqueue(backendResponse)

            //Act & Assert
            val response = webTestClient.get()
                .uri { uriBuilder ->
                    uriBuilder.path("/repositories")
                        .queryParam("username", "username")
                        .build()
                }
                .header(HttpHeader.ACCEPT.name, MediaType.APPLICATION_JSON_VALUE)
                .exchange()

            response.expectStatus().isEqualTo(HttpStatus.NOT_FOUND)

            verify(properties, times(1)).githubUrl
            verify(properties, times(1)).githubToken
        }


    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun `Given username and header when the response returned by the server doesn't match with the defined structure then return an internal server error`() =
        runTest {
            val serverUrl = mockBackEnd.url("/").toString()

            doReturn(serverUrl).whenever(properties).githubUrl
            doReturn(GITHUB_TOKEN).whenever(properties).githubToken

            val repositoryBranchResponse = RepositoryFixture.createRepositoryBranchesResponse()
            val repositoryBranchesList = listOf(repositoryBranchResponse)
            val response = ObjectMapper().writeValueAsString(repositoryBranchesList)

            val backendResponse = MockResponse()
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .setBody(response)
            mockBackEnd.enqueue(backendResponse)

            //Act & Assert
            val webClientResponse = webTestClient.get()
                .uri { uriBuilder ->
                    uriBuilder.path("/repositories")
                        .queryParam("username", "username")
                        .build()
                }
                .header(HttpHeader.ACCEPT.name, MediaType.APPLICATION_JSON_VALUE)
                .exchange()

            webClientResponse.expectStatus().isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)

            verify(properties, times(1)).githubUrl
            verify(properties, times(1)).githubToken
        }
}