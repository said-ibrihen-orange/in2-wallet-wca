package es.in2.wallet.model

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import id.walt.credentials.w3c.W3CIssuer // Import the actual class
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class W3CIssuerDeserializer : StdDeserializer<W3CIssuer>(W3CIssuer::class.java) {
    private val log: Logger = LogManager.getLogger(W3CIssuerDeserializer::class.java)
    override fun deserialize(parser: JsonParser, ctxt: DeserializationContext): W3CIssuer {
        val node: JsonNode = parser.codec.readTree(parser)
        // Add your logic here to extract properties from the JSON and create a W3CIssuer object
        // For example:
        val id = node.get("id").asText()
        log.warn(node)
        val mutableNode = node.get("mutable")
        val mutable = mutableNode?.asBoolean() ?: false
        // Additional properties as needed
        return W3CIssuer(id, mutable)
    }
}