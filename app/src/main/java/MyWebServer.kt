import android.content.Context
import android.util.Log
import fi.iki.elonen.NanoHTTPD
import java.io.IOException
import java.io.InputStream
import java.util.Locale

class MyWebServer(private val context: Context, private val port: Int) : NanoHTTPD(port) {

    private val TAG = "MyWebServer"

    override fun serve(session: IHTTPSession): Response {
        val uri = session.uri
        Log.d(TAG, "Serving URI: $uri")

        val assetManager = context.assets
        val mimeType = getMimeType(uri)
        try {
            val fileUri = if (uri == "/") "/index.html" else uri
            val inputStream: InputStream = assetManager.open(fileUri.removePrefix("/"))
            return newFixedLengthResponse(
                Response.Status.OK,
                mimeType,
                inputStream,
                -1
            )
        } catch (e: IOException) {
            Log.e(TAG, "Error serving URI: $uri", e)
            return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "404 Not Found")
        }
    }

    private fun getMimeType(uri: String): String {
        val extension = uri.substringAfterLast(".").lowercase(Locale.getDefault())
        return when (extension) {
            "html" -> "text/html"
            "css" -> "text/css"
            "js" -> "text/javascript"
            "png" -> "image/png"
            "jpg", "jpeg" -> "image/jpeg"
            "gif" -> "image/gif"
            else -> "application/octet-stream"
        }
    }

    override fun start(timeout: Int, daemon: Boolean) {
        try {
            super.start(timeout, daemon)
        } catch (e: IOException) {
            Log.e(TAG, "Error starting web server", e)
        }
    }
}