package com.imcys.deeprecopy.common.viewmodel.info

interface IViewModelHandle<S : UiState, I : UiIntent> {

    fun handleEvent(event: I, state: S)
}
