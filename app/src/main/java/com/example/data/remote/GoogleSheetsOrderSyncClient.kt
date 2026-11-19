package com.example.data.remote

import android.content.Context
import com.example.BuildConfig
import com.example.data.local.ImageStorageHelper
import com.example.data.model.CustomerOrder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class RemoteOrderRecord(
    val orderNumber: String,
    val customerName: String,
    val customerPhone: String,
    val deliveryAddress: String,
    val itemsJson: String,
    val totalAmountInr: Double,
    val paymentMode: String,
    val upfrontPaidInr: Double,
    val balanceDueOnDeliveryInr: Double,
    val upiTransactionRef: String,
    val screenshotBase64: String,
    val status: String,
    val adminNote: String,
    val createdAt: Long,
    val updatedAt: Long
)

interface OrderSyncTransport {
    val isConfigured: Boolean
    suspend fun pushOrder(order: CustomerOrder, screenshotBase64: String): Result<Unit>
    suspend fun fetchOrders(): Result<List<RemoteOrderRecord>>
    suspend fun decodeRemoteScreenshot(orderNumber: String, screenshotBase64: String): String?
}

class GoogleSheetsOrderSyncClient(
    private val context: Context,
    private val webAppUrlProvider: () -> String = { BuildConfig.ORDER_SYNC_WEB_APP_URL },
    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()
) : OrderSyncTransport {

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    override val isConfigured: Boolean
        get() = webAppUrlProvider().trim().toHttpUrlOrNull() != null

    override suspend fun pushOrder(order: CustomerOrder, screenshotBase64: String): Result<Unit> {
        val baseUrl = webAppUrlProvider().trim()
        val parsedUrl = baseUrl.toHttpUrlOrNull()
            ?: return Result.failure(IllegalStateException("ORDER_SYNC_WEB_APP_URL is not configured"))

        return withContext(Dispatchers.IO) {
            try {
                val payload = JSONObject().apply {
                    put("action", "upsert_order")
                    put("orderNumber", order.orderNumber)
                    put("customerName", order.customerName)
                    put("customerPhone", order.customerPhone)
                    put("deliveryAddress", order.deliveryAddress)
                    put("itemsJson", order.itemsJson)
                    put("totalAmountInr", order.totalAmountInr)
                    put("paymentMode", order.paymentMode)
                    put("upfrontPaidInr", order.upfrontPaidInr)
                    put("balanceDueOnDeliveryInr", order.balanceDueOnDeliveryInr)
                    put("upiTransactionRef", order.upiTransactionRef)
                    put("screenshotBase64", screenshotBase64)
                    put("status", order.status)
                    put("adminNote", order.adminNote)
                    put("createdAt", order.createdAt)
                    put("updatedAt", order.updatedAt)
                }

                val request = Request.Builder()
                    .url(parsedUrl)
                    .post(payload.toString().toRequestBody(jsonMediaType))
                    .build()

                httpClient.newCall(request).execute().use { response ->
                    val body = response.body?.string().orEmpty()
                    if (!response.isSuccessful) {
                        return@withContext Result.failure(
                            IllegalStateException("Sync server returned HTTP ${response.code}")
                        )
                    }
                    if (body.isNotBlank() && body.trimStart().startsWith("{")) {
                        val respJson = JSONObject(body)
                        if (!respJson.optBoolean("ok", true)) {
                            return@withContext Result.failure(
                                IllegalStateException(respJson.optString("error", "Order sync failed"))
                            )
                        }
                    }
                }
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun fetchOrders(): Result<List<RemoteOrderRecord>> {
        val baseUrl = webAppUrlProvider().trim()
        val parsedUrl = baseUrl.toHttpUrlOrNull()
            ?: return Result.failure(IllegalStateException("ORDER_SYNC_WEB_APP_URL is not configured"))

        val listUrl = parsedUrl.newBuilder()
            .addQueryParameter("action", "list_orders")
            .build()

        return withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url(listUrl)
                    .get()
                    .build()

                httpClient.newCall(request).execute().use { response ->
                    val body = response.body?.string().orEmpty()
                    if (!response.isSuccessful) {
                        return@withContext Result.failure(
                            IllegalStateException("Fetch failed with HTTP ${response.code}")
                        )
                    }
                    val root = JSONObject(body)
                    if (!root.optBoolean("ok", true)) {
                        return@withContext Result.failure(
                            IllegalStateException(root.optString("error", "Could not fetch orders"))
                        )
                    }
                    val array = root.optJSONArray("orders") ?: JSONArray()
                    val records = buildList {
                        for (i in 0 until array.length()) {
                            val obj = array.optJSONObject(i) ?: continue
                            val orderNum = obj.optString("orderNumber", "").trim()
                            if (orderNum.isBlank()) continue
                            add(
                                RemoteOrderRecord(
                                    orderNumber = orderNum,
                                    customerName = obj.optString("customerName", ""),
                                    customerPhone = obj.optString("customerPhone", ""),
                                    deliveryAddress = obj.optString("deliveryAddress", ""),
                                    itemsJson = obj.optString("itemsJson", "[]"),
                                    totalAmountInr = obj.optDouble("totalAmountInr", 0.0),
                                    paymentMode = obj.optString("paymentMode", "UPI_FULL"),
                                    upfrontPaidInr = obj.optDouble("upfrontPaidInr", 0.0),
                                    balanceDueOnDeliveryInr = obj.optDouble("balanceDueOnDeliveryInr", 0.0),
                                    upiTransactionRef = obj.optString("upiTransactionRef", ""),
                                    screenshotBase64 = obj.optString("screenshotBase64", ""),
                                    status = obj.optString("status", "PENDING_CONFIRMATION"),
                                    adminNote = obj.optString("adminNote", ""),
                                    createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                                    updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())
                                )
                            )
                        }
                    }
                    Result.success(records)
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun decodeRemoteScreenshot(orderNumber: String, screenshotBase64: String): String? {
        return ImageStorageHelper.saveBase64Image(context, orderNumber, screenshotBase64)
    }
}
