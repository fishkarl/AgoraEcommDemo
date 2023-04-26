package com.example.agoraecomdemo.model

sealed interface LiveInfoResult{
    object Loading : LiveInfoResult
    object Error : LiveInfoResult
    data class Data(val data:List<LiveInfo>) : LiveInfoResult

}