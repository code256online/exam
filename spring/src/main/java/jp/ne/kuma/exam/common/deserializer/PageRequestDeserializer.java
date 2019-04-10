package jp.ne.kuma.exam.common.deserializer;

import java.io.IOException;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.MissingNode;

/**
 * org.springframework.data.domain.PageRequest の JSON デシリアライザ
 *
 * @author Mike
 * @see org.springframework.data.domain.PageRequest
 *
 */
public class PageRequestDeserializer extends JsonDeserializer<PageRequest> {

  @Override
  public PageRequest deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {

    ObjectMapper mapper = (ObjectMapper) p.getCodec();
    JsonNode jsonNode = mapper.readTree(p);

    return PageRequest.of(readJsonNode(jsonNode, "page").asInt(),
        readJsonNode(jsonNode, "size").asInt(),
        mapper.convertValue(readJsonNode(jsonNode, "sort"), Sort.class));
  }

  public static JsonNode readJsonNode(JsonNode jsonNode, String field) {
    return jsonNode.has(field) ? jsonNode.get(field) : MissingNode.getInstance();
  }
}
