package io.github.vishalsharma7nov.kmpproto.example

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.vishalsharma7nov.kmpproto.ProtoClientConfig
import io.github.vishalsharma7nov.kmpproto.errors.ProtoClientError
import io.github.vishalsharma7nov.kmpproto.errors.isProtoClientError
import io.github.vishalsharma7nov.kmpproto.generated.createApi
import kotlinx.coroutines.launch

/**
 * Demo flags — mock mode uses generated protobuf codecs from `user.proto`
 * (in-process MockEngine, no network). Set [USE_MOCK_SERVER] to false and
 * update [liveConfig] to call a real API.
 */
const val USE_MOCK_SERVER: Boolean = false

val liveConfig = ProtoClientConfig(
    baseUrl = "https://api.example.com",
    getHeaders = { mapOf("Authorization" to "Bearer demo-token") },
)

@Composable
fun App() {
    MaterialTheme {
        HomeScreen()
    }
}

@Composable
fun HomeScreen() {
    var result by remember { mutableStateOf("Tap the button to call getUser") }
    val scope = rememberCoroutineScope()
    val api = remember {
        val config = if (USE_MOCK_SERVER) {
            ProtoClientConfig(
                baseUrl = "http://mock.local",
                httpClient = createDemoMockHttpClient(),
            )
        } else {
            liveConfig
        }
        createApi(config)
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("kmp-proto example", style = MaterialTheme.typography.headlineSmall)
        Text(
            if (USE_MOCK_SERVER) "Mock mode ON (proto codecs, no network)"
            else "Live mode → ${liveConfig.baseUrl}",
        )
        Button(
            onClick = {
                scope.launch {
                    result = try {
                        val user = api.userService.getUser(mapOf("ids" to "2"))
                        user.toString()
                    } catch (e: Throwable) {
                        if (isProtoClientError(e)) {
                            val err = e as ProtoClientError
                            "Error: ${err.code} — ${err.message}"
                        } else {
                            "Error: ${e.message}"
                        }
                    }
                }
            },
        ) {
            Text("Call getUser")
        }
        Text(result)
    }
}
