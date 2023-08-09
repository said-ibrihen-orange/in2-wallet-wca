package es.in2.wallet.model

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import id.walt.credentials.w3c.W3CContext

class W3CContextDeserializer : StdDeserializer<W3CContext>(W3CContext::class.java) {
    override fun deserialize(parser: JsonParser, ctxt: DeserializationContext): W3CContext {
        val node: JsonNode = parser.codec.readTree(parser)
        // Add your logic here to extract properties from the JSON and create a W3CContext object
        // For example:
        val contextValue = node.asText()
        return W3CContext(contextValue)
    }
}