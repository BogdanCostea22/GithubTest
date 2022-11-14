package ro.githubdemo.demo.usecases

import kotlinx.coroutines.flow.Flow
import org.springframework.http.HttpHeaders

abstract class UseCaseStructure<V, R> {
    abstract fun validateRequest(headers: HttpHeaders?)
    abstract suspend fun executeBusinessLogicInternal(param: V): Flow<R>

    suspend fun execute(param: V, headers: HttpHeaders? = null): Flow<R> {
        validateRequest(headers)
        return executeBusinessLogicInternal(param)
    }
}
