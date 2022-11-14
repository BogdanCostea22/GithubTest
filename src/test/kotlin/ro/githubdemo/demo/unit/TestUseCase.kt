package ro.githubdemo.demo.unit

import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import ro.githubdemo.demo.controllers.CustomAppException
import ro.githubdemo.demo.usecases.GetRepositoryUseCase
import ro.githubdemo.demo.usecases.contract.RepositoryService
import ro.githubdemo.demo.usecases.contract.model.Branch
import ro.githubdemo.demo.usecases.contract.model.RepositoryData

@ExtendWith(MockitoExtension::class)
class TestUseCase {
    companion object {
        private val ACCEPTED_HEADER = HttpHeaders().apply {
            accept = mutableListOf(MediaType.APPLICATION_JSON)
        }
        private const val USERNAME = "username"
    }

    private lateinit var underTest: GetRepositoryUseCase

    lateinit var mockedRepositoryService: RepositoryService

    @BeforeEach
    fun initialise() {
        mockedRepositoryService = mock()
        underTest = GetRepositoryUseCase(mockedRepositoryService)
    }

    @AfterEach
    fun checkInteraction() {
    }

    @ParameterizedTest
    @ValueSource(strings = ["param1", "param2"])
    fun `Catch exception when not pass headers`(
        requestParameter: String
    ) = runBlocking {
        val exception = assertThrows<CustomAppException.InvalidHeader> {
            underTest.execute(requestParameter, null)
        }

        assertEquals(exception::class, CustomAppException.InvalidHeader::class)
    }

    @ParameterizedTest
    @ValueSource(strings = ["param1", "param2"])
    fun `Catch exception when wrong was received`(
        parameter: String
    ) = runBlocking {
        val headers = HttpHeaders()
        headers.accept = mutableListOf(MediaType.APPLICATION_XML)
        val exception = assertThrows<CustomAppException.InvalidHeader> {
            underTest.execute(parameter, null)
        }

        assertEquals(exception::class, CustomAppException.InvalidHeader::class)
    }

    @Test
    fun `Test when receive the right header and the service thrown an exception after trying to fetch repositories`() =
        runBlocking {
            val username = "username"
            doThrow(CustomAppException.GithubConnectionProblem()).whenever(mockedRepositoryService)
                .getAllRepositories(eq(username))
            val exception = assertThrows<CustomAppException.GithubConnectionProblem> {
                underTest.execute(username, ACCEPTED_HEADER)
            }

            assertEquals(exception::class, CustomAppException.GithubConnectionProblem::class)
        }

    @Test
    fun `Test when receive the right header and the service thrown an exception after trying to fetch the repository branches`(
        @Mock repositoryData: RepositoryData
    ) =
        runBlocking {
            //arrange
            val username = "username"
            val mockedFlow = flow {
                emit(repositoryData)
            }
            doReturn(mockedFlow).whenever(mockedRepositoryService).getAllRepositories(eq(username))
            doThrow(CustomAppException.FailedToFetchRepositoryBranches()).whenever(mockedRepositoryService)
                .getBranchesFor(
                    eq(username), anyOrNull()
                )

            //act

            val exception = assertThrows<CustomAppException.FailedToFetchRepositoryBranches> {
                underTest.execute(username, ACCEPTED_HEADER).collect()
            }

            //assert
            assertEquals(exception::class, CustomAppException.FailedToFetchRepositoryBranches::class)
        }


    @Test
    fun `Test when successfully fetch one repository and the associated branches`() =
        runBlocking {
            //arrange
            val repositoryData: RepositoryData = mock {
                on { login } doReturn USERNAME
                on { name } doReturn "RepositoryName"
            }

            val repositoryBranch: Branch = Branch(
                name = "BranchName",
                lastCommitSha = "sha"
            )

            val mockedRepositoryFlow = flow {
                emit(repositoryData)
            }

            doReturn(mockedRepositoryFlow).whenever(mockedRepositoryService).getAllRepositories(eq(USERNAME))
            doReturn(listOf(repositoryBranch)).whenever(mockedRepositoryService)
                .getBranchesFor(
                    any(), any()
                )

            //act
            val dataFlow = underTest.execute(USERNAME, ACCEPTED_HEADER)

            //assert
            val repositoryResponse = dataFlow.onCompletion {
                if(it != null) {
                    throw RuntimeException("The flow completed with some problems!")
                }
            }.toList()


            assertEquals(repositoryResponse.size, 1)
            assertEquals(repositoryResponse[0].name, repositoryData.name)

        }

}