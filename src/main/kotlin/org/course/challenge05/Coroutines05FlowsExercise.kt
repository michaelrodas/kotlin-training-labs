package org.course.challenge05

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*

data class NewsItem(val id: Long, val headline: String) {
    companion object {
        operator fun invoke(id: Long) = NewsItem(id, "headline #$id")
    }
}

/**
 * Simple in memory dao, keeping all its items in a MutableList
 */
class NewsFeedDao(val news: MutableList<NewsItem>) : MutableList<NewsItem> by news {
    fun findByIdGreaterThan(id: Long): List<NewsItem> = news.filter { it.id > id }
}

/**
 * NewsFeedService that offers Flows of NewsItems needed for
 * - Exercise C
 * - Exercise D
 */
class NewsFeedService(val dao: NewsFeedDao) {

    /**
     * Exercise C:
     * Now we are ready to implement a flow that interacts with a resource, like a data access object.
     * Implement an infinite flow that initially fetches all NewsItem via NewsFeedDao.findByIdGreaterThan
     * and then keeps on polling for new items in intervals of 200ms
     * Hint 1: To make this flow work you need to keep track of the latest NewsItem id that was fetched. For that you
     * can use a var latestId in the flow builder, which is perfectly thread-safe.
     * Hint 2: It might help defining a nested method (e.g.: tailrec suspend fun fetch()) that recursively fetches
     * NewsItem based on the latestId it accesses in the surrounding scope. Once fetched it also delay's for 200ms before recursing.
     */
    fun pollingNewsFeedFlow(): Flow<NewsItem> = flow {
        var latestId = -1L
        tailrec suspend fun fetch() { //this means kotlin optimizes recursion
            val newsItems = dao.findByIdGreaterThan(latestId)
            newsItems.forEach {
                emit(it)
                latestId = it.id

            }
            delay(200)
            fetch()
        }
        fetch()

    }


    /**
     * Exercise D:
     * Let's combine a flow with a SharedFlow.
     * Implement an infinite flow that initially fetches all NewsItem via NewsFeedDao.findByIdGreaterThan
     * and then waits for the SharedFlow to emit a notification before fetching new items.
     * Hint 1: the implementation is almost the same like the one in Exercise D (@sample NewsFeedService#channelTriggeredNewsFeedFlow).
     * The only difference is that instead of consuming notifications from the Channel call collect on the SharedFlow,
     * so when a notification is received a subsequent fetch can be initiated.
     */
    fun sharedFlowTriggeredNewsFeedFlow(sharedFlow: Flow<String>): Flow<NewsItem> = flow {
        var latestId = -1L
        suspend fun fetch() { //this means kotlin optimizes recursion
            val newsItems = dao.findByIdGreaterThan(latestId)
            newsItems.forEach {
                emit(it)
                latestId = it.id

            }
        }
        fetch()
        sharedFlow.collect { fetch()}
    }
}
