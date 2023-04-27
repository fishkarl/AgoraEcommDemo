package com.example.agoraecomdemo.repo

import com.example.agoraecomdemo.model.LiveInfo
import com.example.agoraecomdemo.model.LiveInfoResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject


class LiveInfoRepository
@Inject constructor()
{

	private val mChannelFlow = MutableStateFlow<LiveInfoResult>(LiveInfoResult.Loading)

	init {
		var result = listOf<LiveInfo>(
			LiveInfo(uid = "1001" , cname= "DT01"),
			LiveInfo(uid = "1002" , cname= "DT02"),
			LiveInfo(uid = "1003" , cname= "DT03"),
			LiveInfo(uid = "1004" , cname= "DT04"),
			LiveInfo(uid = "1005" , cname= "DT05")
			)

		mChannelFlow.value = LiveInfoResult.Data(result)

		}

	fun getFeed(): Flow<LiveInfoResult> = mChannelFlow
}