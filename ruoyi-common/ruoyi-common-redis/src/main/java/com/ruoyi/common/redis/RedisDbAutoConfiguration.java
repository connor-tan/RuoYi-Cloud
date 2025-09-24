package com.ruoyi.common.redis;

import com.ruoyi.common.core.utils.SpringUtils;
import com.ruoyi.common.redis.common.FastJson2JsonRedisSerializer;
import com.ruoyi.common.redis.connection.JedisDbConnectionConfiguration;
import com.ruoyi.common.redis.connection.LettuceDbConnectionConfiguration;
import com.ruoyi.common.redis.connection.PropertiesRedisDbConnectionDetails;
import com.ruoyi.common.redis.service.RedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisConnectionDetails;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Role;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.ruoyi.common.redis.common.RedisBeanNames.*;
import static com.ruoyi.common.redis.common.SerializationUtils.jackson2JsonRedisSerializer;
import static com.ruoyi.common.redis.common.SerializationUtils.stringSerializer;

/**
 * Redis多数据源配置，参考源码：LettuceConnectionConfiguration
 * {@link RedisAutoConfiguration}
 *
 * @author Emily
 * @since 2021/07/11
 */
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
@AutoConfiguration(before = RedisAutoConfiguration.class)
@EnableConfigurationProperties(RedisDbProperties.class)
@ConditionalOnProperty(prefix = RedisDbProperties.PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
@Import({LettuceDbConnectionConfiguration.class, JedisDbConnectionConfiguration.class})
public class RedisDbAutoConfiguration implements InitializingBean, DisposableBean {
    private static final Logger LOG = LoggerFactory.getLogger(RedisDbAutoConfiguration.class);
    private final RedisDbProperties redisDbProperties;

    public RedisDbAutoConfiguration( RedisDbProperties redisDbProperties) {
        this.redisDbProperties = redisDbProperties;
    }

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    @ConditionalOnMissingBean(RedisConnectionDetails.class)
    PropertiesRedisDbConnectionDetails redisConnectionDetails() {
        String defaultConfig = Objects.requireNonNull(redisDbProperties.getDefaultConfig(), "Redis默认标识不可为空");
        PropertiesRedisDbConnectionDetails redisConnectionDetails = null;
        for (Map.Entry<String, RedisProperties> entry : redisDbProperties.getConfig().entrySet()) {
            String key = entry.getKey();
            RedisProperties properties = entry.getValue();
            if (defaultConfig.equals(key)) {
                redisConnectionDetails = new PropertiesRedisDbConnectionDetails(properties);
                SpringUtils.registerSingleton(join(key, REDIS_CONNECT_DETAILS), redisConnectionDetails);
            } else {
                SpringUtils.registerSingleton(join(key, REDIS_CONNECT_DETAILS), new PropertiesRedisDbConnectionDetails(properties));
            }
        }
        return redisConnectionDetails;
    }

    @Bean(name = DEFAULT_REDIS_TEMPLATE)
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    @ConditionalOnMissingBean(name = DEFAULT_REDIS_TEMPLATE)
    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        String defaultConfig = Objects.requireNonNull(redisDbProperties.getDefaultConfig(), "Redis默认标识不可为空");
        RedisTemplate<Object, Object> redisTemplate = null;
        for (Map.Entry<String, RedisProperties> entry : redisDbProperties.getConfig().entrySet()) {
            FastJson2JsonRedisSerializer<Object> serializer = new FastJson2JsonRedisSerializer<>(Object.class);
            String key = entry.getKey();
            RedisTemplate<Object, Object> template = new RedisTemplate<>();
            template.setKeySerializer(stringSerializer());
            template.setValueSerializer(serializer);
            template.setHashKeySerializer(stringSerializer());
            template.setHashValueSerializer(serializer);
            if (defaultConfig.equals(key)) {
                template.setConnectionFactory(redisConnectionFactory);
                redisTemplate = template;
            } else {
                template.setConnectionFactory(SpringUtils.getBean(join(key, REDIS_CONNECTION_FACTORY), RedisConnectionFactory.class));
                template.afterPropertiesSet();
            }
            SpringUtils.registerSingleton(join(key, REDIS_TEMPLATE), template);
        }

        return redisTemplate;
    }

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    @ConditionalOnMissingBean(name = DEFAULT_REDIS_SERVICE)
    @ConditionalOnBean(name = DEFAULT_REDIS_TEMPLATE)
    public RedisService redisService(List<RedisTemplate<Object, Object>> redisTemplates) {
        String defaultConfig = Objects.requireNonNull(redisDbProperties.getDefaultConfig(), "Redis默认标识不可为空");
        RedisService redisService = null;
        for (Map.Entry<String, RedisProperties> entry : redisDbProperties.getConfig().entrySet()) {
            String key = entry.getKey();

            if (defaultConfig.equals(key)) {
                RedisTemplate defaultRedisTemplate = SpringUtils.getBean(DEFAULT_REDIS_TEMPLATE, RedisTemplate.class);
                redisService = new RedisService(defaultRedisTemplate);
            }else {
                RedisTemplate redisTemplate = SpringUtils.getBean(join(key,REDIS_TEMPLATE), RedisTemplate.class);
                RedisService rService = new RedisService(redisTemplate);
                SpringUtils.registerSingleton(join(key, REDIS_SERVICE), rService);
                LOG.info("注册非默认RedisService:{}",join(key, REDIS_SERVICE));
            }
        }
        return redisService;
    }

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    @ConditionalOnMissingBean(name = DEFAULT_STRING_REDIS_TEMPLATE)
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        String defaultConfig = Objects.requireNonNull(redisDbProperties.getDefaultConfig(), "Redis默认标识不可为空");
        StringRedisTemplate stringRedisTemplate = null;
        for (Map.Entry<String, RedisProperties> entry : redisDbProperties.getConfig().entrySet()) {
            String key = entry.getKey();
            StringRedisTemplate template = new StringRedisTemplate();
            template.setKeySerializer(stringSerializer());
            template.setValueSerializer(stringSerializer());
            template.setHashKeySerializer(stringSerializer());
            template.setHashValueSerializer(stringSerializer());
            if (defaultConfig.equals(key)) {
                template.setConnectionFactory(redisConnectionFactory);
                stringRedisTemplate = template;
            } else {
                template.setConnectionFactory(SpringUtils.getBean(join(key, REDIS_CONNECTION_FACTORY), RedisConnectionFactory.class));
                template.afterPropertiesSet();
            }
            SpringUtils.registerSingleton(join(key, STRING_REDIS_TEMPLATE), template);
        }

        return stringRedisTemplate;
    }


    @Override
    public void destroy() {
        LogHolder.LOG.info("<== 【销毁--自动化配置】----Redis数据库多数据源组件【RedisDbAutoConfiguration】");
    }

    @Override
    public void afterPropertiesSet() {
        LogHolder.LOG.info("==> 【初始化--自动化配置】----Redis数据库多数据源组件【RedisDbAutoConfiguration】");
    }

    static class LogHolder {
        private static final Logger LOG = LoggerFactory.getLogger(RedisDbAutoConfiguration.class);
    }
}
