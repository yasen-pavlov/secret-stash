package me.bitnet.secretstash.util.ratelimiter

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class RedisRateLimiterService(
    private val redisTemplate: RedisTemplate<String, Any>,
) {
    /**
     * Check if request is allowed based on rate limits
     *
     * @param userId User identifier for rate limiting
     * @param limit Maximum requests allowed in the time window
     * @param windowSeconds Time window in seconds
     * @return true if request is allowed, false if rate limit exceeded
     */
    fun isAllowed(
        userId: String,
        limit: Int,
        windowSeconds: Int,
    ): Boolean {
        val key = "rate_limit:$userId:${System.currentTimeMillis() / (windowSeconds * 1000)}"
        val count = redisTemplate.opsForValue().increment(key, 1) ?: 1

        // Set expiration if key is new
        if (count == 1L) {
            redisTemplate.expire(key, windowSeconds.toLong(), TimeUnit.SECONDS)
        }

        return count <= limit
    }

    /**
     * Reset rate limit for a specific user (useful for testing)
     *
     * @param userId User identifier to reset limits for
     */
    fun resetLimit(userId: String) {
        val keyPattern = "rate_limit:$userId:*"
        val keys = redisTemplate.keys(keyPattern)
        if (keys.isNotEmpty()) {
            redisTemplate.delete(keys)
        }
    }
}
