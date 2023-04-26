package com.example.agoraecomdemo

import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat
import com.example.agoraecomdemo.ui.FeedScreen
import com.example.agoraecomdemo.ui.theme.AgoraEcomDemoTheme
import dagger.hilt.android.AndroidEntryPoint



object Constants{
    var TAG = "ChannelSwitching"
}

private val permissions = arrayOf(android.Manifest.permission.RECORD_AUDIO, android.Manifest.permission.CAMERA)


@AndroidEntryPoint
class ChannelSwitching : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AgoraEcomDemoTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    UIRequirePermissions(
                        permissions = permissions,
                        onPermissionGranted = {

                        },
                        onPermissionDenied = {
                            AlertScreen(it)
                        }
                    )

                    FeedScreen()
                }
            }
        }


    }
}

@Composable
private fun UIRequirePermissions(
    permissions: Array<String>,
    onPermissionGranted: @Composable () -> Unit,
    onPermissionDenied: @Composable (requester: () -> Unit) -> Unit
) {
    Log.d(Constants.TAG, "UIRequirePermissions: ")
    val context = LocalContext.current

    var grantState by remember {
        mutableStateOf(permissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        })
    }

    if (grantState) onPermissionGranted()
    else {
        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions(),
            onResult = {
                grantState = !it.containsValue(false)
            }
        )
        onPermissionDenied {
            Log.d(Constants.TAG, "launcher.launch")
            launcher.launch(permissions)
        }
    }
}

@Composable
private fun AlertScreen(requester: () -> Unit) {
    val context = LocalContext.current

    Log.d(Constants.TAG, "AlertScreen: ")
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.Red),
        contentAlignment = Alignment.Center
    ) {
        Button(onClick = {
            requestPermissions(
                context as Activity,
                permissions,
                22
            )
            requester()
        }) {
            Icon(Icons.Rounded.Warning, "Permission Required")
            Text(text = "Permission Required")
        }
    }
}


