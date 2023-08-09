import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import id.walt.credentials.w3c.VerifiableCredential // Import the actual class
import id.walt.credentials.w3c.templates.VcTemplate
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class VcTemplateDeserializer : StdDeserializer<VcTemplate>(VcTemplate::class.java) {
    private val log: Logger = LogManager.getLogger(VcTemplateDeserializer::class.java)
    override fun deserialize(parser: JsonParser, ctxt: DeserializationContext): VcTemplate {
        val node: JsonNode = parser.codec.readTree(parser)
        val name = node.get("name").asText()
        val mutable = node.get("mutable").asBoolean()
        // Adjust the following line to get the correct type for "template"
        log.warn(node)
        val template = if (node.has("template")) {
            parser.codec.treeToValue(node.get("template"), VerifiableCredential::class.java)
        } else {
            null // or provide a default value if appropriate
        }

        return VcTemplate(name, template, mutable)
    }
}