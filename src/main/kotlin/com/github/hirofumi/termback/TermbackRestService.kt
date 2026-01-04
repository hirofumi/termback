package com.github.hirofumi.termback

import com.github.hirofumi.termback.TermbackNotifier.NotifyResult
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.FullHttpRequest
import io.netty.handler.codec.http.HttpMethod
import io.netty.handler.codec.http.HttpResponseStatus
import io.netty.handler.codec.http.QueryStringDecoder
import org.jetbrains.ide.BuiltInServerManager
import org.jetbrains.ide.RestService
import org.jetbrains.io.sendPlainText

/**
 * HTTP endpoint for terminal notification requests.
 *
 * This endpoint is unauthenticated because:
 * - The built-in server only accepts connections from localhost
 * - Session IDs (UUID v4) prevent accidental cross-tab interference
 * - Impact is limited to displaying notifications and focusing terminal tabs
 *
 * @see <a href="https://github.com/JetBrains/intellij-community/blob/idea/253.29346.138/platform/built-in-server/src/org/jetbrains/io/BuiltInServer.kt#L111-L119">BuiltInServer binds to 127.0.0.1</a>
 */
class TermbackRestService : RestService() {
    override fun getServiceName(): String = SERVICE_NAME

    override fun isMethodSupported(method: HttpMethod): Boolean = method == HttpMethod.POST

    override fun execute(
        urlDecoder: QueryStringDecoder,
        request: FullHttpRequest,
        context: ChannelHandlerContext,
    ): String? {
        val jsonString = request.content().toString(Charsets.UTF_8)

        val notificationRequest =
            when (val result = TermbackNotificationRequest.parse(jsonString)) {
                is TermbackNotificationRequest.ParseResult.Success -> {
                    result.request
                }

                is TermbackNotificationRequest.ParseResult.Error -> {
                    HttpResponseStatus.BAD_REQUEST.sendPlainText(context.channel(), request, result.reason)
                    return null
                }
            }

        when (TermbackNotifier.getInstance().notify(notificationRequest)) {
            NotifyResult.Accepted -> {
                HttpResponseStatus.ACCEPTED.sendPlainText(context.channel(), request)
            }

            NotifyResult.SessionNotFound -> {
                HttpResponseStatus.NOT_FOUND.sendPlainText(context.channel(), request, "Session not found")
            }
        }
        return null
    }

    companion object {
        private const val SERVICE_NAME = "termback"

        fun getEndpointUrl(): String = "http://localhost:${BuiltInServerManager.getInstance().port}/$PREFIX/$SERVICE_NAME"
    }
}
