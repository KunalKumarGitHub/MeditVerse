package com.learning.meditation.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class LoadingViewModel:ViewModel() {
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> get() = _loading

    fun yesLoading(){
        _loading.value=true
    }
    fun notLoading(){
        _loading.value=false
    }
}