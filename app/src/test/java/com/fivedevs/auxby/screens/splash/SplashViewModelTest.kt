package com.fivedevs.auxby.screens.splash

import com.fivedevs.auxby.screens.base.BaseViewModelTest
import com.google.common.truth.Truth
import org.junit.Before
import org.junit.Test


class SplashViewModelTest : BaseViewModelTest() {

    private lateinit var viewModel: SplashViewModel

    @Before
    fun setUp() {
        viewModel = SplashViewModel(preferencesService)
    }

    @Test
    fun `test on dashboard navigation should br null`() {
        Truth.assertThat(viewModel.onDashboardNavigation.value).isNull()
    }
}