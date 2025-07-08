package com.appversal.appstorys.utils

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsOwner
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.SemanticsPropertyReceiver
import androidx.compose.ui.semantics.getOrNull
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import java.lang.reflect.Field
import kotlin.math.roundToInt
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.view.PixelCopy
import android.view.WindowManager
import android.view.WindowMetrics
import androidx.compose.ui.platform.LocalContext
import com.appversal.appstorys.api.ApiRepository
import com.appversal.appstorys.api.RetrofitClient.apiService
import java.io.File
import java.io.FileOutputStream
import androidx.core.graphics.createBitmap
import com.appversal.appstorys.api.RetrofitClient.mqttApiService
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resumeWithException

val AppstorysViewTagKey = SemanticsPropertyKey<String>("AppstorysViewTagKey")

var SemanticsPropertyReceiver.appstorysViewTagProperty by AppstorysViewTagKey

internal object ViewTreeAnalyzer {
    private const val ANDROID_COMPOSE_VIEW_CLASS_NAME =
        "androidx.compose.ui.platform.AndroidComposeView"

    // Bitmap to store the captured screenshot
    private var screenBitmap: Bitmap? = null

    // Getter for the screenshot
    fun getScreenshot(): Bitmap? = screenBitmap

    private val semanticsOwnerField: Field? by lazy {
        try {
            Class.forName(ANDROID_COMPOSE_VIEW_CLASS_NAME)
                .getDeclaredField("semanticsOwner")
                .apply { isAccessible = true }
        } catch (e: Exception) {
            Log.e("ViewTreeAnalyzer", "Reflection failed: Could not find semanticsOwner field", e)
            null
        }
    }

    /**
     * Analyzes the view tree starting from the root View and generates a JSON representation.
     * Includes both traditional Android Views and Compose Views embedded within.
     * Reports coordinates relative to the application window.
     *
     * @param root The root View of the hierarchy to analyze.
     * @param screenName A name for the screen being analyzed.
     * @return A JsonObject representing the view tree.
     */
    suspend fun analyzeViewRoot(
        root: View,
        screenName: String,
        user_id: String,
        accessToken: String,
        activity: Activity,
        context: Context
    ): JsonObject {
        val resultJson = JsonObject().apply {
            addProperty("name", screenName)
            val children = JsonArray()
            analyzeViewElement(
                root,
                onElementAnalyzed = { children.add(it) },
                activity = activity
            )
            add("children", children)
        }

        val gson = GsonBuilder().setPrettyPrinting().create()
        val formattedJson = gson.toJson(resultJson.getAsJsonArray("children"))
        Log.e(
            "ViewTreeAnalyzer",
            formattedJson
        )

        val screenshot = captureScreenshot(
            view = root,
            activity = activity
        )

        val repository = ApiRepository(context, apiService, mqttApiService) { screenName }

        if (screenshot != null) {
            repository.tooltipIdentify(
                accessToken = accessToken,
                user_id = user_id,
                screenName = screenName,
                childrenJson = formattedJson,
                screenshotFile = screenshot
            )
        }

        return resultJson
    }

    /**
     * Captures a screenshot of the provided view and stores it in the screenBitmap variable.
     *
     * @param view The view to capture.
     */
    private suspend fun captureScreenshot(activity: Activity, view: View): File? {
        return try {
            if (view.width <= 0 || view.height <= 0) {
                Log.e("ViewTreeAnalyzer", "Cannot capture screenshot: View has invalid dimensions")
                return null
            }

            val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            val file = File.createTempFile("screenshot_", ".png", view.context.cacheDir)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // PixelCopy-based screenshot (API 26+)
                suspendCancellableCoroutine<File?> { continuation ->
                    val location = IntArray(2)
                    view.getLocationInWindow(location)
                    val rect = Rect(
                        location[0],
                        location[1],
                        location[0] + view.width,
                        location[1] + view.height
                    )

                    PixelCopy.request(
                        activity.window,
                        rect,
                        bitmap,
                        { result ->
                            if (result == PixelCopy.SUCCESS) {
                                try {
                                    FileOutputStream(file).use { out ->
                                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                                    }
                                    screenBitmap = bitmap
                                    continuation.resume(file, onCancellation = null)
                                } catch (e: Exception) {
                                    Log.e("ViewTreeAnalyzer", "Failed to save screenshot", e)
                                    continuation.resumeWithException(e)
                                }
                            } else {
                                val error = Exception("PixelCopy failed with code $result")
                                Log.e("ViewTreeAnalyzer", error.message ?: "Unknown error")
                                continuation.resumeWithException(error)
                            }
                        },
                        Handler(Looper.getMainLooper())
                    )
                }
            } else {
                // Software rendering fallback (for API < 26)
                val canvas = Canvas(bitmap)
                view.draw(canvas)
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
                screenBitmap = bitmap
                Log.d("ViewTreeAnalyzer", "Screenshot captured successfully (fallback)")
                file
            }
        } catch (e: Exception) {
            Log.e("ViewTreeAnalyzer", "Error capturing screenshot", e)
            screenBitmap = null
            null
        }
    }

    fun getScreenSize(context: Context): Pair<Int, Int> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics: WindowMetrics =
                context.getSystemService(WindowManager::class.java).currentWindowMetrics
            val bounds = windowMetrics.bounds
            Pair(bounds.width(), bounds.height())
        } else {
            val displayMetrics = DisplayMetrics()
            @Suppress("DEPRECATION")
            (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.getMetrics(
                displayMetrics
            )
            Pair(displayMetrics.widthPixels, displayMetrics.heightPixels)
        }
    }

    /**
     * Recursively analyzes a single View element and its children.
     * Reports coordinates relative to the application window.
     *
     * @param view The current View to analyze.
     * @param onElementAnalyzed Callback to receive the JsonObject for the analyzed element.
     */
    private fun analyzeViewElement(
        view: View,
        onElementAnalyzed: (JsonObject) -> Unit,
        activity: Activity
    ) {
        // Skip views with no ID
        val viewId = try {
            if (view.id != View.NO_ID) {
                view.resources.getResourceEntryName(view.id)
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }

        if (viewId != null) {
            // Calculate window-relative position for traditional Views
            val locationInWindow = IntArray(2)
            view.getLocationInWindow(locationInWindow)
            val xInWindow = locationInWindow[0]
            val yInWindow = locationInWindow[1]

            val (screenWidth, screenHeight) = getScreenSize(activity)

            val elementJson = JsonObject().apply {
                addProperty("id", viewId)
                add(
                    "frame",
                    JsonObject().apply {
                        addProperty("x", xInWindow)
                        addProperty("y", yInWindow)
                        addProperty("width", view.width)
                        addProperty("height", view.height)
                        addProperty("screenWidth", screenWidth)
                        addProperty("screenHeight", screenHeight)
                    }
                )
            }

            onElementAnalyzed(elementJson)
        }

        // Recursively analyze children for ViewGroups
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                analyzeViewElement(view.getChildAt(i), onElementAnalyzed, activity = activity)
            }
        }

        // Handle embedded AndroidComposeView
        if (view::class.java.name == ANDROID_COMPOSE_VIEW_CLASS_NAME) {
            analyzeComposeView(view, onElementAnalyzed)
        }
    }

    /**
     * Analyzes the Compose tree within a AndroidComposeView using reflection.
     * WARNING: This uses reflection on internal Compose APIs and is highly fragile.
     * Reports coordinates relative to the application window.
     *
     * @param view The AndroidComposeView to analyze.
     * @param onElementAnalyzed Callback to receive the JsonObject for the analyzed element.
     */
    private fun analyzeComposeView(
        view: View,
        onElementAnalyzed: (JsonObject) -> Unit
    ) {
        @Suppress("TooGenericExceptionCaught")
        try {
            val semanticsOwner = semanticsOwnerField?.get(view) as? SemanticsOwner

            if (semanticsOwner != null) {
                // Analyze the root semantics node of the Compose tree
                analyzeSemanticsNode(
                    semanticsOwner.rootSemanticsNode,
                    view.context,
                    onElementAnalyzed
                )
            } else {
                Log.w(
                    "ViewTreeAnalyzer",
                    "Could not get SemanticsOwner from ComposeView via reflection."
                )
            }

        } catch (ex: Exception) {
            // Catching and swallowing exceptions here with the Compose view handling in case
            // something changes in the future that breaks the expected structure being accessed
            // through reflection here. If anything goes wrong within this block, prefer to continue
            // processing the remainder of the view tree as best we can.
            Log.e(
                "ViewTreeAnalyzer",
                "Error processing Compose layout via reflection: ${ex.message}"
            )
        }
    }

    /**
     * Recursively analyzes a SemanticsNode and its children.
     * Reports coordinates relative to the application window.
     *
     * @param semanticsNode The current SemanticsNode to analyze.
     * @param context The Android Context (needed for density if converting units).
     * @param onElementAnalyzed Callback to receive the JsonObject for the analyzed element.
     */
    private fun analyzeSemanticsNode(
        semanticsNode: SemanticsNode,
        context: Context,
        onElementAnalyzed: (JsonObject) -> Unit
    ) {
        // Attempt to extract a valid ID from the Semantics config
        val nodeId = semanticsNode.config.getOrNull(AppstorysViewTagKey)

        // Skip nodes that do not have a valid ID
        if (nodeId != null) {
            val xInWindow = semanticsNode.positionInWindow.x.roundToInt()
            val yInWindow = semanticsNode.positionInWindow.y.roundToInt()
            val widthPx = semanticsNode.size.width
            val heightPx = semanticsNode.size.height

            val elementJson = JsonObject().apply {
                addProperty("id", nodeId)

                // Position and size
                add("frame", JsonObject().apply {
                    addProperty("x", xInWindow)
                    addProperty("y", yInWindow)
                    addProperty("width", widthPx)
                    addProperty("height", heightPx)
                })
            }

            onElementAnalyzed(elementJson)
        }

        // Recursively analyze children nodes
        semanticsNode.children.forEach { child ->
            analyzeSemanticsNode(child, context, onElementAnalyzed)
        }
    }

}
