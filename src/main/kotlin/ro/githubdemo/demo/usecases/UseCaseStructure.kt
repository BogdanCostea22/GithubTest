package ro.githubdemo.demo.usecases

import kotlinx.coroutines.flow.Flow

abstract class UseCaseStructure<V, T, R> {
    abstract fun validateRequest(headers: Map<String, String>)
    abstract suspend fun executeBusinessLogicInternal(param: V, requestBody: T?): Flow<R>

    suspend fun execute(headers: Map<String, String>, param: V, requestBody: T? = null): Flow<R> {
        validateRequest(headers)
        return executeBusinessLogicInternal(param, requestBody)
    }
}
