package jp.ne.kuma.exam.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.security.jackson2.CoreJackson2Module;
import org.springframework.security.jackson2.SecurityJackson2Modules;
import org.springframework.security.web.jackson2.WebJackson2Module;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jp.ne.kuma.exam.common.deserializer.OrderDeserializer;
import jp.ne.kuma.exam.common.deserializer.PageRequestDeserializer;
import jp.ne.kuma.exam.common.deserializer.SortDeserializer;

@Configuration
@EnableRedisHttpSession
@Profile("!junit")
public class SessionConfig {

  @Bean
  public RedisConnectionFactory redisConnectionFactory() {
    return new LettuceConnectionFactory();
  }

  @Bean
  public RedisSerializer<Object> springSessionDefaultRedisSerializer() {

    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModules(new CoreJackson2Module(), new WebJackson2Module(), new JavaTimeModule(), new Jdk8Module());
    mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
    mapper.registerModules(SecurityJackson2Modules.getModules(this.getClass().getClassLoader()));
    mapper.addMixIn(PageRequest.class, PageRequestMixIn.class);
    mapper.addMixIn(Sort.class, SortMixIn.class);
    mapper.addMixIn(Order.class, OrderMixIn.class);
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    return new GenericJackson2JsonRedisSerializer(mapper);
  }

  @JsonTypeInfo(use = Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
  @JsonDeserialize(using = PageRequestDeserializer.class)
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public abstract class PageRequestMixIn {}

  @JsonTypeInfo(use = Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
  @JsonDeserialize(using = SortDeserializer.class)
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public abstract class SortMixIn {}

  @JsonTypeInfo(use = Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
  @JsonDeserialize(using = OrderDeserializer.class)
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public abstract class OrderMixIn {}
}
