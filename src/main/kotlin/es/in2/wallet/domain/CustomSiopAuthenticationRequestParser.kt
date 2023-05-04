package es.in2.wallet.domain

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.net.URI

class CustomSiopAuthenticationRequestParser {

    private val log: Logger = LogManager.getLogger(CustomSiopAuthenticationRequestParser::class.java)

    fun parse(siopUrl: String): CustomSiopAuthenticationRequest {
        log.info("parsing... $siopUrl")
        val uri = URI(siopUrl.removePrefix("?"))
        val query = uri.query

        log.info("query = $query")

        val queryParams = query.removePrefix("?").split("&")
            .associate {
                val (key, value) = it.split("=")
                key to value
            }

        val scope = queryParams["scope"]
            //?.removePrefix("[")
            //?.removeSuffix("]")
            ?.split(",") ?: emptyList()
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