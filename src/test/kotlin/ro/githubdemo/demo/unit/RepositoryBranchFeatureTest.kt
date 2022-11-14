package ro.githubdemo.demo.unit

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.*
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
class RepositoryBranchFeatureTest {
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
    fun `Given username and repository when remote endpoint returns a list of branches then map and return them`() =
        runTest {
            //Arrange
            val serverUrl = mockBackEnd.url("/").toString()
            println("SERVER_URL $serverUrl")

            doReturn(serverUrl).whenever(appProperties).githubUrl
            doReturn(GITHUB_TOKEN).whenever(appProperties).githubToken

            val repositoryBranchResponse = RepositoryFixture.createRepositoryBranchesResponse()
            val repositoryBranchesList = listOf(repositoryBranchResponse)
            val response = ObjectMapper().writeValueAsString(repositoryBranchesList)

            val backendResponse = MockResponse()
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .setBody(response)

            mockBackEnd.enqueue(backendResponse)

            //Act & Assert
            val branches = underTest.getBranchesFor("username", RepositoryFixture.REPOSITORY_NAME)

            branches.forEach { repositoryBranch ->
                Assertions.assertEquals(repositoryBranch.name, RepositoryFixture.REPOSITORY_BRANCH_NAME)
                Assertions.assertEquals(repositoryBranch.lastCommitSha, RepositoryFixture.REPOSITORY_BRANCH_SHA)
            }
            verify(appProperties, times(1)).githubUrl
            verify(appProperties, times(1)).githubToken
        }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun `Given username and repository when remote endpoint return an 404 status code then CustomAppException-UserNotFound is returned`() =
        runTest {
            //Arrange
            val serverUrl = mockBackEnd.url("/").toString()
            doReturn(serverUrl).whenever(appProperties).githubUrl
            doReturn(GITHUB_TOKEN).whenever(appProperties).githubToken

            val backendResponse = MockResponse()
                .setResponseCode(HttpStatus.NOT_FOUND.value())
                .addHeader("Content-Type", "application/json; charset=utf-8")

            mockBackEnd.enqueue(backendResponse)

            //Act & Assert
            assertThrows<CustomAppException.UserNotFound> {
                underTest.getBranchesFor("username", "repository")
            }
            verify(appProperties, times(1)).githubUrl
            verify(appProperties, times(1)).githubToken
        }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun `Given username and repository when remote endpoint return an response with status code 4xx or 5xx(different from 404) then CustomAppException-ConnectionProblem is returned`() =
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

            //Act & Assert
            repeat(2) {
                assertThrows<CustomAppException.GithubConnectionProblem> {
                    underTest.getBranchesFor("username", "repository")
                }
            }
            verify(appProperties, times(2)).githubUrl
            verify(appProperties, times(2)).githubToken
        }
}
