package es.in2.wallet.model

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import id.walt.credentials.w3c.W3CCredentialSchema
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class W3CCredentialSchemaDeserializer : JsonDeserializer<W3CCredentialSchema>() {
    private val log: Logger = LogManager.getLogger(W3CCredentialSchemaDeserializer::class.java)
    override fun deserialize(parser: JsonParser, ctxt: DeserializationContext): W3CCredentialSchema {
        val node: JsonNode = parser.codec.readTree(parser)

        // Extract the necessary fields from the JSON node
        val id = node.get("id").asText()
        val type = node.get("type").asText()
        val properties = parseProperties(node)

        // Create and return the W3CCredentialSchema instance
        return W3CCredentialSchema(id, type, properties)
    }

    private fun parseProperties(node: JsonNode): Map<String, Any?> {
        val properties = mutableMapOf<String, Any?>()
        val fieldNames = node.fieldNames()

        while (fieldNames.hasNext()) {
            val fieldName = fieldNames.next()
            if (fieldName != "id" && fieldName != "type") {
                properties[fieldName] = parsePropertyValue(node.get(fieldName))
            }
        }

        return properties
    }

    private fun parsePropertyValue(jsonNode: JsonNode): Any? {
        // Implement your logic here to convert the JSON node to the appropriate data type
        // You might need to handle different JSON node types (string, number, object, etc.)
        // and convert them accordingly.
        log.error(jsonNode)
        return null // Return the parsed property value
    }
}