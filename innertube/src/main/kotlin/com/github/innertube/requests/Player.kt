package com.github.innertube.requests

import com.github.innertube.Innertube
import com.github.innertube.models.Context
import com.github.innertube.models.PlayerResponse
import com.github.innertube.models.YouTubeClient
import com.github.innertube.models.bodies.PlayerBody
import com.github.innertube.utils.runCatchingNonCancellable
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable

@Serializable
data class AudioStream(
    val url: String,
    val bitrate: Long,
    val mimeType: String
)

@Serializable
data class PipedResponse(
    val audioStreams: List<AudioStream>
)

suspend fun Innertube.player(videoId: String) = runCatchingNonCancellable {
    <<<<<<< jules-9347389017490971799-15cf4eea
    val pipedInstances = listOf(
        "https://pipedapi.kavin.rocks",
        "https://pipedapi.adminforge.de",
        "https://piped-api.garudalinux.org"
    )

    for (instance in pipedInstances) {
        try {
            val audioStreams = client.get("$instance/streams/$videoId") {
                contentType(ContentType.Application.Json)
            }.body<PipedResponse>().audioStreams.filter { it.url.isNotEmpty() }

            if (audioStreams.isNotEmpty()) {
                val adaptiveFormats = audioStreams.map { stream ->
                    val isOpus = stream.mimeType.contains("opus", ignoreCase = true)
                    val itag = if (isOpus) 251 else 140
                    PlayerResponse.StreamingData.AdaptiveFormat(
                        itag = itag,
                        mimeType = stream.mimeType,
                        bitrate = stream.bitrate,
                        averageBitrate = stream.bitrate,
                        contentLength = null,
                        audioQuality = null,
                        approxDurationMs = null,
                        lastModified = null,
                        loudnessDb = null,
                        audioSampleRate = null,
                        url = stream.url
                    )
                }
                return@runCatchingNonCancellable PlayerResponse(
                    playabilityStatus = PlayerResponse.PlayabilityStatus("OK"),
                    playerConfig = null,
                    streamingData = PlayerResponse.StreamingData(adaptiveFormats),
                    videoDetails = PlayerResponse.VideoDetails(videoId)
                )
            }
        } catch (e: Exception) {
            // Ignore and try the next instance
        }
    }

    val safePlayerResponse = client.post(PLAYER) {
        setBody(
            PlayerBody(
                context = YouTubeClient.WEB_REMIX.toContext(),
                videoId = videoId
            )
        )
        mask("playabilityStatus.status,playerConfig.audioConfig,streamingData.adaptiveFormats,videoDetails.videoId")
    }.body<PlayerResponse>()

    safePlayerResponse
}
=======

@Serializable
data class AudioStream(
    val url: String,
    val bitrate: Long,
    val mimeType: String = "audio/mp4",
    val codec: String = ""
)

@Serializable
data class PipedResponse(val audioStreams: List<AudioStream>)

val pipedInstances = listOf(
    "https://pipedapi.adminforge.de",
    "https://pipedapi.kavin.rocks",
    "https://piped-api.garudalinux.org"
)

var audioStreams: List<AudioStream>? = null
for (instance in pipedInstances) {
    audioStreams = runCatching {
        client.get("$instance/streams/$videoId") {
            contentType(ContentType.Application.Json)
        }.body<PipedResponse>().audioStreams
    }.getOrNull()
    if (!audioStreams.isNullOrEmpty()) break
}

if (audioStreams.isNullOrEmpty()) {
    // Piped failed — try YouTube directly as last resort
    return@runCatchingNonCancellable client.post(PLAYER) {
        setBody(
            PlayerBody(
                context = YouTubeClient.WEB_REMIX.toContext().copy(
                    thirdParty = Context.ThirdParty(
                        embedUrl = "https://www.youtube.com/watch?v=$videoId"
                    )
                ),
                videoId = videoId
            )
        )
        mask("playabilityStatus.status,playerConfig.audioConfig,streamingData.adaptiveFormats,videoDetails.videoId")
    }.body<PlayerResponse>()
}

// Build response entirely from Piped streams
PlayerResponse(
playabilityStatus = PlayerResponse.PlayabilityStatus(status = "OK"),
playerConfig = null,
videoDetails = PlayerResponse.VideoDetails(videoId = videoId),
streamingData = PlayerResponse.StreamingData(
adaptiveFormats = audioStreams.map { stream ->
    PlayerResponse.StreamingData.AdaptiveFormat(
        itag = if (stream.mimeType.contains("opus")) 251 else 140,
        mimeType = stream.mimeType,
        bitrate = stream.bitrate,
        averageBitrate = stream.bitrate,
        contentLength = null,
        audioQuality = "AUDIO_QUALITY_MEDIUM",
        approxDurationMs = null,
        lastModified = null,
        loudnessDb = null,
        audioSampleRate = null,
        url = stream.url
    )
}
)
)
}
>>>>>>> main