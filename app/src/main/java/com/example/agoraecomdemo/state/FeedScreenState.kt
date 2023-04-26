package com.example.agoraecomdemo.state

sealed interface FeedScreenState {
	object Loading : FeedScreenState
	object Empty : FeedScreenState
	data class Data(val items: List<LiveItemState>) : FeedScreenState
}
