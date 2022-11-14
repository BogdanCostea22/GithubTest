package ro.githubdemo.demo.unit

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import ro.githubdemo.demo.controllers.CustomAppException
import ro.githubdemo.demo.usecases.RepositoryWithBranches
import ro.githubdemo.demo.usecases.UseCaseStructure
import ro.githubdemo.demo.usecases.contract.model.Branch
import java.nio.charset.StandardCharsets
import java.util.stream.Stream


@ExtendWith(SpringExtension::class)
@WebFluxTest
class RestControllerTest {

    @MockBean
    lateinit var getRepositoryUseCase: UseCaseStructure<String, RepositoryWithBranches>

    @Autowired
    lateinit var webTestClient: WebTestClient

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun test() = runTest {
        //Arrange
        val branch = Branch(
            name = "branchName",
            lastCommitSha = "Commit_sha"
        )

        val repositoryWithBranches = RepositoryWithBranches(
            name = "repositoryName",
            branches = listOf(branch),
            login = "userName"
        )

        val responseFlow = flowOf(repositoryWithBranches)
        doReturn(responseFlow).whenever(getRepositoryUseCase).execute(any(), anyOrNull())

        //Assert
        webTestClient.get()
            .uri { uriBuilder ->
                uriBuilder.path("/repositories")
                    .queryParam("username", "username")
                    .build()
            }
            .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .exchange()
            .expectStatus()
            .isOk

        verify(getRepositoryUseCase, times(1)).execute(any(), any())
    }

    companion object {
        // Make sure to use the correct return type Stream<Arguments>
        @JvmStatic
        fun input(): Stream<Arguments?>? {
            return Stream.of(
                Arguments.of(CustomAppException.InvalidHeader()),
                Arguments.of(CustomAppException.ConnectionProblem()),
                Arguments.of(CustomAppException.NotFoundException()),
                Arguments.of(CustomAppException.EndpointServerError()),
                Arguments.of(CustomAppException.UserNotFound()),
            )
        }
    }

    fun CustomAppException.getErrorMessage(): String =
        ObjectMapper().writeValueAsString(this)


    @OptIn(ExperimentalCoroutinesApi::class)
    @ParameterizedTest
    @MethodSource("input")
    fun testWhenUseCaseReturnsAnNotFoundException(
        customAppException: CustomAppException
    ) = runTest {
        //Arrange

        val notFoundException = customAppException
        doThrow(notFoundException).whenever(getRepositoryUseCase).execute(any(), anyOrNull())

        //Assert
        val response = webTestClient.get()
            .uri { uriBuilder ->
                uriBuilder.path("/repositories")
                    .queryParam("username", "username")
                    .build()
            }
            .exchange()

        response.expectStatus().isEqualTo(notFoundException.status)
        val returnedBody = response.expectBody()
        val byteArray = returnedBody.returnResult()
        val errorBody = String(byteArray.responseBodyContent!!, StandardCharsets.UTF_8)
        assertEquals(errorBody, notFoundException.getErrorMessage())
        verify(getRepositoryUseCase, times(1)).execute(any(), any())
    }
}
