package com.gify.ui.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.gify.domain.interactor.GetGifListUseCase
import com.gify.domain.model.Gif
import com.gify.ui.mapper.GifModelMapper
import com.gify.ui.viewstate.ServerDataState
import com.rules.RxSchedulerRule
import com.util.mock
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.notification.Failure
import org.mockito.ArgumentCaptor
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnit

class GifListViewModelTest {
    @Rule
    @JvmField
    var mockitoRule = MockitoJUnit.rule()!!

    private lateinit var gifListViewModel: GifListViewModel

    @Mock
    lateinit var gifListUseCase: GetGifListUseCase

    @Mock
    var observer: Observer<ServerDataState> = mock()

    @Rule
    @JvmField
    var testSchedulerRule: RxSchedulerRule = RxSchedulerRule()

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()


    @Before
    fun setup() {
        gifListViewModel = GifListViewModel(gifListUseCase)
        gifListViewModel.liveGifData.observeForever(observer)

    }

    @Test
    fun `view model is ready for test`() {
        assertNotNull(gifListViewModel)
    }

    @Test
    fun empty() {
        gifListViewModel.loadNextPage("egypt", 0)
        verifyNoMoreInteractions(gifListUseCase)
    }

    @Test
    fun `search gif should return lis of gifs`() {
        //given
        val list = ArrayList<Gif>()
        list.add(Gif("1", "t1", "prev_url1", "orig_url1"))
        list.add(Gif("2", "t2", "prev_url2", "orig_url2"))
        val expected= ServerDataState.Success(GifModelMapper.transform(list))
        //when
        gifListViewModel.viewState.value = ServerDataState.Success(GifModelMapper.transform(list))
       // then
        gifListViewModel.loadNextPage("egypt", 0)
        val captor = ArgumentCaptor.forClass(ServerDataState::class.java)
        captor.run {
            verify(observer, times(1)).onChanged(capture())
            assertEquals(expected, value)
        }
    }


    @Test
    fun `no internet connection`() {
        //given
        val list = ArrayList<Gif>()
        list.add(Gif("1", "t1", "prev_url1", "orig_url1"))
        list.add(Gif("2", "t2", "prev_url2", "orig_url2"))
        val networkconnection = com.gify.data.exceptions.Failure.NetworkConnection
        val expected= ServerDataState.Error(networkconnection)
        //when
        gifListViewModel.viewState.value = ServerDataState.Error(networkconnection)
        // then
        gifListViewModel.loadNextPage("egypt", 0)
        val captor = ArgumentCaptor.forClass(ServerDataState::class.java)
        captor.run {
            verify(observer, times(1)).onChanged(capture())
            assertEquals(expected, value)
        }
    }


}