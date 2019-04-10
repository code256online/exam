package jp.ne.kuma.exam.common.deserializer;

import java.io.IOException;

import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.NullHandling;
import org.springframework.data.domain.Sort.Order;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * org.springframework.data.domain.Sort.Order の JSON デシリアライザ
 *
 * @author Mike
 * @see org.springframework.data.domain.Sort.Order
 *
 */
public class OrderDeserializer extends JsonDeserializer<Order> {

  @Override
  public Order deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {

    ObjectMapper mapper = (ObjectMapper) p.getCodec();
    JsonNode jsonNode = mapper.readTree(p);
    String property = PageRequestDeserializer.readJsonNode(jsonNode, "property").asText();
    Direction direction = Direction.valueOf(PageRequestDeserializer.readJsonNode(jsonNode, "direction").asText());
    boolean ignoreCase = PageRequestDeserializer.readJsonNode(jsonNode, "ignoreCase").asBoolean();
    NullHandling nullHandlingHint = NullHandling.valueOf(PageRequestDeserializer.readJsonNode(jsonNode, "nullHandling").asText());

    Order order = new Order(direction, property, nullHandlingHint);
    if (ignoreCase) {
      order = order.ignoreCase();
    }

    return order;
  }
}
