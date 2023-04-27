package com.example.agoraecomdemo.ui

import android.util.Log
import android.view.TextureView
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberImagePainter
import coil.request.ImageRequest
import com.example.agoraecomdemo.FeedViewModel
import com.example.agoraecomdemo.state.FeedItemState
import com.example.agoraecomdemo.state.FeedScreenState
import com.example.agoraecomdemo.state.LiveItemState


object Constants{
    var TAG = "FeedScreen"
}

@Composable
fun FeedScreen() {

    val viewModel = hiltViewModel<FeedViewModel>()
    val state by viewModel.observeFeedScreenState()
    val joinStates = viewModel.observeJoinState()

    Box(
        modifier = Modifier
            .background(color = Color.Black)
            .fillMaxSize()
    ) {
        when (state) {
            is FeedScreenState.Data -> FeedPager(
                items = (state as FeedScreenState.Data).items,
                joinStates = joinStates,
                onLikeClick = {viewModel.likeOrDislike(it)},
                onPageChanged = {viewModel.onPageChanged(it)},
                setRemoteView = { tv : TextureView ,uid : String, cname : String-> viewModel.setRemoteView(tv,uid,cname)}
            )
            FeedScreenState.Empty -> EmptyPager()
            FeedScreenState.Loading -> Loading()
        }
    }
}

@Preview
@Composable
private fun previewFeedPager(){
    Box(
        modifier = Modifier
            .background(color = Color.Black)
            .fillMaxSize()
    )
    val items = listOf<LiveItemState>(
        LiveItemState("1001","DKT01","1"),
        LiveItemState("1002","DKT02","2"),
        LiveItemState("1003","DKT03","3"),
        )

    //FeedPager(items = items,rtcEngine){}
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FeedPager(
    items: List<LiveItemState>,
    joinStates: Map<String,Boolean>,
    onLikeClick: (String) -> Unit,
    onPageChanged: (Int) -> Unit,
    setRemoteView : (TextureView,String,String) -> Unit

) {
    val pageCount = items.size
    val startIndex = 0
    val pagerState = rememberPagerState(initialPage = 0)

    VerticalPager(
        pageCount = Int.MAX_VALUE,
        state = pagerState,
//        beyondBoundsPageCount = 1
    ) { index ->
        val page = (index - startIndex).floorMod(pageCount)
        Log.e(Constants.TAG,"Page : $index")
        PagerItem(
            itemState = items[page],
            isSelected = page == (pagerState.currentPage - startIndex).floorMod(pageCount),
            onLikeClick = onLikeClick,
            onPageChanged = onPageChanged,
            page = page,
            joinState = joinStates[items[page].cname],
            setRemoteViews = setRemoteView
        )
    }
}

@Composable
private fun PagerItem(
    itemState: LiveItemState,
    isSelected: Boolean,
    onLikeClick: (String) -> Unit,
    onPageChanged: (Int) -> Unit,
    page : Int,
    joinState : Boolean?,
    setRemoteViews : (TextureView,String,String) -> Unit
) {
    if(isSelected){
        onPageChanged.invoke(page)
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(shape = RoundedCornerShape(16.dp)),
    ) {
        if(joinState == true){
            FeedVideoPlayer(
                uid = itemState.uid,
                cname = itemState.cname,
                setRemoteView = setRemoteViews,

            )
        }
        RightControlsBlock(itemState, onLikeClick)
        BottomBlock(itemState)
        RightTopBlock(itemState)
    }
}


@Composable
fun FeedVideoPlayer(
    uid: String,
    cname: String,
    setRemoteView :  (TextureView,String,String) -> Unit
) {

//    var previewImageState by remember { mutableStateOf(true) }

        val context = LocalContext.current
        AndroidView(
            factory = {
                TextureView(context).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    setRemoteView.invoke(this,uid,cname)
                }},
            modifier = Modifier
                .fillMaxSize()
        )

}

@Composable
private fun EmptyPager() {
    Log.e(Constants.TAG,"EMPTY PAGER")
    Box(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "EMPTY",
            textAlign = TextAlign.Center,
            color = Color.White,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}


@Composable
private fun Loading() {
    Box(modifier = Modifier.fillMaxSize()) {
        CircularProgressIndicator(
            modifier = Modifier
                .size(64.dp)
                .align(Alignment.Center),
            color = Color.White
        )
    }
}

@Composable
private fun RightControlsBlock(
    itemState: LiveItemState,
    onLikeClick: (String) -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .width(80.dp)
                .padding(16.dp)
                .align(Alignment.CenterEnd)
        ) {

          //  UploaderAvatar(itemState.uploaderAvatarUrl)

            Spacer(modifier = Modifier.height(16.dp))

            LikeIcon(isLiked = false) {
                onLikeClick
            }

            Spacer(modifier = Modifier.height(4.dp))

           // LikesCount(itemState.likesCount)
        }
    }
}

@Composable
private fun UploaderAvatar(avatarUrl: String) {
    Image(
        painter = rememberImagePainter(
            request = ImageRequest.Builder(LocalContext.current).data(avatarUrl).build(),
        ),
        contentDescription = "",
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .border(2.dp, Color.White, CircleShape)
    )
}

@Composable
private fun LikeIcon(
    isLiked: Boolean,
    onClick: () -> Unit
) {
    var isLikedd = remember{ mutableStateOf(false) }

    Icon(
        imageVector = Icons.Outlined.Favorite,
        tint = if (isLikedd.value) Color.Red else Color.White,
        contentDescription = "",
        modifier = Modifier
            .size(48.dp)
            .clickable(onClick = {
                isLikedd.value = !isLikedd.value;
            })
    )
}

@Composable
private fun LikesCount(likesCount: Int) {
    Text(
        text = likesCount.toString(),
        textAlign = TextAlign.Center,
        color = Color.White,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun BottomBlock(itemState: LiveItemState) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {

            Text(
                text = itemState.uid,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = itemState.cname,
                color = Color.White
            )
        }
    }
}

@Composable
private fun RightTopBlock(itemState: LiveItemState) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = itemState.numberOfLive,
            color = Color.White,
            modifier = Modifier
                .align(Alignment.TopStart)
                .clip(RoundedCornerShape(8.dp))
                .padding(8.dp)
                .background(Color.Black)
        )
    }
}

@Composable
private fun PreviewImage(
    previewImageState: Boolean,
    previewUrl: String
) {

    val ir = ImageRequest.Builder(LocalContext.current).data(previewUrl).build()
    AnimatedVisibility(
        visible = previewImageState,
        modifier = Modifier.fillMaxSize(),
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Image(
            contentScale = ContentScale.Crop,
            painter = rememberImagePainter(request = ir),
            contentDescription = ""
        )
    }
}


@Composable
private fun PlayPauseIcon(playPauseState: Boolean) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        AnimatedVisibility(
            visible = !playPauseState,
            modifier = Modifier
                .size(56.dp)
                .align(Alignment.Center),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Icon(
                imageVector = Icons.Filled.PlayCircleFilled,
                contentDescription = "",
                tint = Color.White
            )
        }
    }
}

// c =  a / b
// r = a - c * b
private fun Int.floorMod(other:Int):Int = when(other){
    0 -> this
    else -> this - floorDiv(other) * other
}

private fun LogI(msg:String) = Log.i(Constants.TAG,msg)
