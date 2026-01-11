package com.bhaskar.pixelwalls.data.repository

import com.bhaskar.pixelwalls.domain.model.AiAspectRatio
import com.bhaskar.pixelwalls.domain.repository.AiRepository
import com.bhaskar.pixelwalls.utils.Constants
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.add
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import kotlin.io.encoding.Base64

class AiRepositoryImpl(
    private val httpClient: HttpClient
): AiRepository {

    private val API_KEY = "API_KEY"
    private val URL = "${Constants.GEMINI_API_URL}$API_KEY"


    override suspend fun generateImage(prompt: String, aspectRatio: AiAspectRatio): Result<ByteArray> {

        return try {

            println("Prompt: $prompt")

            val requestBody = buildJsonObject {
                putJsonArray("contents") {
                    addJsonObject {
                        put("role", "user")
                        putJsonArray("parts") {
                            addJsonObject { put("text", prompt) }
                        }
                    }
                }
                putJsonObject("generationConfig") {
                    putJsonArray("responseModalities") {
                        add("IMAGE")
                    }
                    putJsonObject("imageConfig") {
                        put("aspectRatio", aspectRatio.value)
                    }
                }
            }

            println("Request Body: $requestBody")

            val response = httpClient.post(URL) {
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }

            println("Response Status: ${response.status}")
            println("Response Status Code: ${response.status.value}")

            val responseText = response.bodyAsText()
            println("Response Body: $responseText")

            if(response.status.isSuccess()) {

                val responseText = response.bodyAsText()

                println("Gemini Response: $responseText")

                val json = Json { ignoreUnknownKeys = true }

                val candidates = if (responseText.trim().startsWith("[")) {
                    json.parseToJsonElement(responseText).jsonArray.firstOrNull()?.jsonObject?.get("candidates")
                } else {
                    json.parseToJsonElement(responseText).jsonObject["candidates"]
                }

                val base64Image = candidates?.jsonArray?.getOrNull(0)
                    ?.jsonObject?.get("content")
                    ?.jsonObject?.get("parts")
                    ?.jsonArray?.getOrNull(0)
                    ?.jsonObject?.get("inlineData")
                    ?.jsonObject?.get("data")?.jsonPrimitive?.content

                if(base64Image != null) {
                    Result.success(Base64.decode(base64Image))
                } else {
                    println("Could not find image data in response")
                    Result.failure(Exception("Could not find image data in response"))
                }
            } else {
                val errorMsg = "API Error: ${response.status.value} - ${response.status.description}"
                println(errorMsg)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            println("Exception occurred: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }

    }
}