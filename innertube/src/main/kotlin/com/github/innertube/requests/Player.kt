package com.github.innertube.requests

import com.github.innertube.Innertube
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

suspend fun Innertube.player(videoId: String) = runCatchingNonCancellable {

    @Serializable
    data class AudioStream(
        val url: String,
        val bitrate: Long,
        val mimeType: String,
        val codec: String? = null,
        val quality: String? = null
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
        return@runCatchingNonCancellable client.post(PLAYER) {
            setBody(
                PlayerBody(
                    context = YouTubeClient.WEB_REMIX.toContext(visitorData = visitorData),
                    videoId = videoId
                )
            )
            mask("playabilityStatus.status,playerConfig.audioConfig,streamingData.adaptiveFormats,videoDetails.videoId")
        }.body<PlayerResponse>()
    }

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
                    audioQuality = stream.quality ?: "AUDIO_QUALITY_MEDIUM",
                    approxDurationMs = null,
                    lastModified = null,
                    loudnessDb = null,
                    audioSampleRate = if (stream.mimeType.contains("opus")) 48000 else 44100,
                    url = stream.url
                )
            }
        )
    )
}
