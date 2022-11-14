package ro.githubdemo.demo.unit

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpStatus
import ro.githubdemo.demo.config.AppProperties
import ro.githubdemo.demo.controllers.CustomAppException
import ro.githubdemo.demo.service.RepositoryServiceImpl
import ro.githubdemo.demo.unit.fixture.RepositoryFixture


@SpringBootTest
class UserRepositoryFeatureTest {
    companion object {
        private const val GITHUB_TOKEN = "GITHUB_TOKEN"
    }

    lateinit var mockBackEnd: MockWebServer

    @MockBean
    private lateinit var appProperties: AppProperties

    @Autowired
    private lateinit var underTest: RepositoryServiceImpl

    @BeforeEach
    fun setUp() {
        mockBackEnd = MockWebServer()
        mockBackEnd.start()
    }

    @AfterEach
    fun tearDown() {
        mockBackEnd.shutdown()

        verifyNoMoreInteractions(appProperties)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun `Given username when remote endpoint returns a list of repositories then map and return them`() = runTest {
        //Arrange
        val serverUrl = mockBackEnd.url("/").toString()
        println("SERVER_URL $serverUrl")

        doReturn(serverUrl).whenever(appProperties).githubUrl
        doReturn(GITHUB_TOKEN).whenever(appProperties).githubToken

        val repositoryResponse = RepositoryFixture.createRepositoryResponse()
        val repositoryList = listOf(repositoryResponse)
        val response = ObjectMapper().writeValueAsString(repositoryList)

        val backendResponse = MockResponse()
            .addHeader("Content-Type", "application/json; charset=utf-8")
            .setBody(response)

        mockBackEnd.enqueue(backendResponse)

        //Act
        underTest.getAllRepositories("username").collect { repositoryData ->
            assertEquals(repositoryData.name, repositoryResponse.name)
            assertEquals(repositoryData.login, repositoryResponse.owner?.login)
        }

        verify(appProperties, times(1)).githubUrl
        verify(appProperties, times(1)).githubToken
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun `Given username when remote endpoint return an 404 status code then CustomAppException-UserNotFound is returned`() =
        runTest {
            //Arrange
            val serverUrl = mockBackEnd.url("/").toString()
            doReturn(serverUrl).whenever(appProperties).githubUrl
            doReturn(GITHUB_TOKEN).whenever(appProperties).githubToken

            val backendResponse = MockResponse()
                .setResponseCode(HttpStatus.NOT_FOUND.value())
                .addHeader("Content-Type", "application/json; charset=utf-8")

            mockBackEnd.enqueue(backendResponse)

            //Act
            assertThrows<CustomAppException.UserNotFound> {
                underTest.getAllRepositories("username").collect()
            }
            verify(appProperties, times(1)).githubUrl
            verify(appProperties, times(1)).githubToken
        }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun `Given username when remote endpoint return an response with status code 4xx or 5xx(different from 404) then CustomAppException-GithubConnectionProblem is returned`() =
        runTest {
            //Arrange
            val serverUrl = mockBackEnd.url("/").toString()
            doReturn(serverUrl).whenever(appProperties).githubUrl
            doReturn(GITHUB_TOKEN).whenever(appProperties).githubToken

            val backendResponse = MockResponse()
                .setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .addHeader("Content-Type", "application/json; charset=utf-8")

            val secondErrorResponse = MockResponse()
                .setResponseCode(HttpStatus.FORBIDDEN.value())
                .addHeader("Content-Type", "application/json; charset=utf-8")

            mockBackEnd.enqueue(backendResponse)
            mockBackEnd.enqueue(secondErrorResponse)

            //Act
            repeat(2) {
                assertThrows<CustomAppException.GithubConnectionProblem> {
                    underTest.getAllRepositories("username").collect()
                }
            }

            verify(appProperties, times(2)).githubUrl
            verify(appProperties, times(2)).githubToken
        }
}
