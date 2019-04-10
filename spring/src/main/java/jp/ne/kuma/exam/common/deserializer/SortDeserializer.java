package jp.ne.kuma.exam.common.deserializer;

import java.io.IOException;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * org.springframework.data.domain.Sort の JSON デシリアライザ
 *
 * @author Mike
 * @see org.springframework.data.domain.Sort
 *
 */
public class SortDeserializer extends JsonDeserializer<Sort> {

  @Override
  public Sort deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {

    ObjectMapper mapper = (ObjectMapper) p.getCodec();
    JsonNode jsonNode = mapper.readTree(p);
    @SuppressWarnings("unchecked")
    List<Order> orders = mapper.convertValue(PageRequestDeserializer.readJsonNode(jsonNode, "orders"), List.class);

    return Sort.by(orders);
  }
}
