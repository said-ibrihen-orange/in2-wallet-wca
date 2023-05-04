package es.in2.wallet.domain

import java.net.URI

class CustomSiopAuthenticationRequestParser {

    fun parse(siopUrl: String): CustomSiopAuthenticationRequest {
        val uri = URI(siopUrl)
        val query = uri.query
        val queryParams = query.split("&")
            .associate {
                val (key, value) = it.split("=")
                key to value
            }

        val scope = queryParams["scope"]?.split(",") ?: emptyList()
        val responseType = queryParams["response_type"] ?: ""
        val responseMode = queryParams["response_mode"] ?: ""
        val clientId = queryParams["client_id"] ?: ""
        val redirectUri = queryParams["redirect_uri"] ?: ""
        val state = queryParams["state"] ?: ""
        val nonce = queryParams["nonce"] ?: ""
        val presentationDefinition = queryParams["presentation_definition"]
        val presentationDefinitionUri = queryParams["presentation_definition_uri"]

        return CustomSiopAuthenticationRequest(
            scope = scope,
            responseType = responseType,
            responseMode = responseMode,
            clientId = clientId,
            redirectUri = redirectUri,
            state = state,
            nonce = nonce,
            presentationDefinition = presentationDefinition,
            presentationDefinitionUri = presentationDefinitionUri
        )
    }

}