package com.example.agoraecomdemo

import android.content.Context
import android.util.Log
import android.view.TextureView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.agoraecomdemo.model.LiveInfo
import com.example.agoraecomdemo.model.LiveInfoResult
import com.example.agoraecomdemo.repo.LiveInfoRepository
import com.example.agoraecomdemo.state.FeedScreenState
import com.example.agoraecomdemo.state.LiveItemState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcConnection
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig
import io.agora.rtc2.RtcEngineEx
import io.agora.rtc2.video.VideoCanvas
import io.agora.rtc2.video.VideoCanvas.RENDER_MODE_FIT
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import javax.inject.Inject

@HiltViewModel
class FeedViewModel
@Inject constructor(
	private val mLiveInfoRepository: LiveInfoRepository,
	@ApplicationContext private val context : Context
) : ViewModel() {

	private val mFeedScreenState = mutableStateOf<FeedScreenState>(FeedScreenState.Loading)
	private var mChannelJoinState = mutableStateMapOf<String,Boolean>()
	private lateinit var rtcEngineEx : RtcEngineEx


	init {
		viewModelScope.launch {
			mLiveInfoRepository.getFeed()
				.collect { result ->
					mFeedScreenState.value = when (result) {
						is LiveInfoResult.Data -> {
							FeedScreenState.Data(result.data.mapIndexed { index, liveInfo ->
								generateFeedItemState(liveInfo, index, result.data)
							})
						}
						LiveInfoResult.Error -> FeedScreenState.Empty
						LiveInfoResult.Loading -> FeedScreenState.Loading
					}
					rtcEngineEx = initRtcEngine(context,"0b11eaee339d4ef98d47945acd6e461d") as RtcEngineEx


				}
		}
	}

	private fun generateFeedItemState(liveInfo: LiveInfo, index: Int, list: List<LiveInfo>) = LiveItemState(
		uid = liveInfo.uid?:"",
		cname=liveInfo.cname?:"",
		numberOfLive = "${index + 1}/${list.size}"
	)

	fun observeFeedScreenState(): State<FeedScreenState> = mFeedScreenState
	fun observeJoinState(): Map<String,Boolean> = mChannelJoinState


	fun likeOrDislike(videoId: String) {
//		viewModelScope.launch {
//			mFeedLikeRepository.likeOrDislike(videoId).collect {}
//		}
	}

	fun setRemoteView(textureView : TextureView,uid:String, cname : String){
		val ret = rtcEngineEx.setupRemoteVideoEx(VideoCanvas(textureView,VideoCanvas.RENDER_MODE_HIDDEN,1000) ,createConnection(uid,cname))
		Log.e("FVM", "setRemoteView ret : $ret")
//		rtcEngineEx.muteAllRemoteAudioStreamsEx(true,)
//		rtcEngineEx.muteRemoteAudioStreamEx(1000,false,createConnection(uid,cname))
	}

	//TODO: Audio Playback control
	fun muteChannel(){

	}


	fun onPageChanged(page : Int){
		Log.e("FVM", "Current selected page is : $page")
		//TODO:JoinChannel and JoinNextChannel
		val items =(mFeedScreenState.value as FeedScreenState.Data).items
		val item = items[page]
		val connection = createConnection(item.uid,item.cname)
		if(mChannelJoinState[item.cname] != true){
			joinChannelEx(connection, createMediaOptions(), object : IRtcEngineEventHandler(){
				override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
					super.onJoinChannelSuccess(channel, uid, elapsed)
					Log.e("FVM","OnJoinChannel succ : $channel" )
					mChannelJoinState[item.cname] = true
				}
			})
		}
		if(page + 1 < items.size){
			val itemNext = items[page + 1]
			val connectionNext = createConnection(itemNext.uid,itemNext.cname)
			if(mChannelJoinState[itemNext.cname] != true) {
				joinChannelEx(
					connectionNext,
					createMediaOptions(),
					object : IRtcEngineEventHandler() {
						override fun onJoinChannelSuccess(
							channel: String?,
							uid: Int,
							elapsed: Int
						) {
							super.onJoinChannelSuccess(channel, uid, elapsed)
							Log.e("FVM", "OnJoinChannel succ : $channel")
							mChannelJoinState[itemNext.cname] = true

						}
					})
			}
		}


		//TODO : LeavePreviousChannel
	}

	private fun createConnection(uid:String, cname:String): RtcConnection = RtcConnection(cname,uid.toInt())

	private fun createEngineConfig(current: Context, appID : String) = RtcEngineConfig().apply {
		mAppId = appID
		mEventHandler = rtcEventHandler()
		mContext = current
	}

	private fun createMediaOptions(): ChannelMediaOptions = ChannelMediaOptions().apply {
		channelProfile = Constants.CHANNEL_PROFILE_LIVE_BROADCASTING
		clientRoleType = Constants.CLIENT_ROLE_AUDIENCE
		autoSubscribeAudio = false
		autoSubscribeVideo = true
	}

	private fun initRtcEngine(current:Context, appID:String): RtcEngine {
		val rtcEngine = RtcEngine.create(current,appID, rtcEventHandler())
		rtcEngine.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING)
		rtcEngine.setClientRole(Constants.CLIENT_ROLE_AUDIENCE)
		rtcEngine.enableVideo()
		return rtcEngine
	}


	private fun rtcEventHandler() : IRtcEngineEventHandler {
		return object: IRtcEngineEventHandler(){
			override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
				super.onJoinChannelSuccess(channel, uid, elapsed)
			}
		}
	}

	private fun joinChannelEx(connection: RtcConnection,mediaOptions: ChannelMediaOptions,eventHandler: IRtcEngineEventHandler){
		viewModelScope.launch(Dispatchers.IO){
			rtcEngineEx.joinChannelEx(null,connection,mediaOptions,eventHandler)
		}
	}

}