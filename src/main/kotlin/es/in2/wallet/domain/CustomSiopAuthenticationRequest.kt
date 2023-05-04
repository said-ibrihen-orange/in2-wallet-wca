package es.in2.wallet.domain

import java.net.URI

class CustomSiopAuthenticationRequest(
    private val scope: List<String>,
    private val responseType: String,
    private val responseMode: String,
    private val clientId: String,
    private val redirectUri: String,
    private val state: String,
    private val nonce: String,
    private val presentationDefinition: String? = null,
    private val presentationDefinitionUri: String? = null
) {
    // getters for all properties
    fun getScope(): List<String> = scope
    fun getResponseType(): String = responseType
    fun getResponseMode(): String = responseMode
    fun getClientId(): String = clientId
    fun getRedirectUri(): String = redirectUri
    fun getState(): String = state
    fun getNonce(): String = nonce
    fun getPresentationDefinition(): String? = presentationDefinition
    fun getPresentationDefinitionUri(): String? = presentationDefinitionUri

    fun prettyPrint(): String {
        return if (presentationDefinition == null || presentationDefinitionUri == null) {
            "?scope=$scope&response_type=$responseType&response_mode=$responseMode&client_id=$clientId" +
                    "&state=$state&nonce=$nonce&redirect_uri=$redirectUri"
        } else {
            "?scope=$scope&presentation_definition=$presentationDefinition" +
                    "&presentation_definition_uri=$presentationDefinitionUri&response_type=$responseType" +
                    "&response_mode=$responseMode&client_id=$clientId&state=$state&nonce=$nonce" +
                    "&redirect_uri=$redirectUri"
        }
    }




    }
