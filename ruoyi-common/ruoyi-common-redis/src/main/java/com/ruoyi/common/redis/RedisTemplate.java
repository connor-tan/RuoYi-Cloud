package com.ruoyi.common.redis;

import org.springframework.data.redis.connection.RedisConnection;

/**
 * 自定义的redisTemplate，主要是为了能够在使用过程中动态切换数据库
 * 切换方式请查看{@link com.ruoyi.common.redis.service.RedisService#setDB(int)}
 * @author Connor

 */
public class RedisTemplate<k, v> extends org.springframework.data.redis.core.RedisTemplate<k, v> {
    public static ThreadLocal<Integer> REDIS_DB_INDEX = new ThreadLocal<>();

    @Override
    protected RedisConnection preProcessConnection(RedisConnection connection, boolean existingConnection) {
        try {
            Integer dbIndex = REDIS_DB_INDEX.get();
            //如果设置了dbIndex
            if (dbIndex != null) {
                connection.select(dbIndex);
            }
        } finally {
            REDIS_DB_INDEX.remove();
        }
        return super.preProcessConnection(connection, existingConnection);
    }


}
