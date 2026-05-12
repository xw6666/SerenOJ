package top.wjr.serenoj.shiro;

import cn.hutool.extra.spring.SpringUtil;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.CollectionUtils;
import top.wjr.serenoj.utils.RedisUtils;

import java.util.*;

public class ShiroCache<K, V> implements Cache<K, V> {

    private long cacheLiveTime;

    private String cacheKeyPrefix;

    private RedisUtils redisUtils;

    public ShiroCache(long cacheLiveTime, String cacheKeyPrefix, RedisUtils redisUtils) {
        this.cacheLiveTime = cacheLiveTime;
        this.cacheKeyPrefix = cacheKeyPrefix;
        this.redisUtils = redisUtils;
    }

    private String getKey(K key) {
        String userId;
        if (key instanceof PrincipalCollection) {
            AccountProfile accountProfile = (AccountProfile) ((PrincipalCollection) key).getPrimaryPrincipal();
            userId = accountProfile.getUid();
        } else {
            userId = key.toString();
        }

        return this.cacheKeyPrefix + userId;
    }

    @Override
    public V get(K key) throws CacheException {
        if (!this.redisUtils.hasKey(this.getKey(key))) {
            return null;
        }
        Object o = this.redisUtils.get(this.getKey(key));
        return (V) o;
    }

    @Override
    public V put(K key, V value) throws CacheException {
        this.redisUtils.set(this.getKey(key), value, this.cacheLiveTime);
        return value;
    }

    @Override
    public V remove(K key) throws CacheException {
        if (!this.redisUtils.hasKey(this.getKey(key))) {
            return null;
        }
        this.redisUtils.del(this.getKey(key));
        return null;
    }

    @Override
    public void clear() throws CacheException {
        RedisUtils redisUtils = SpringUtil.getBean(RedisUtils.class);
        Set<String> keys = redisUtils.keys(this.cacheKeyPrefix + "*");
        if (null != keys && keys.size() > 0) {
            redisUtils.del(keys);
        }
    }

    @Override
    public Set<K> keys() {
        return (Set<K>) redisUtils.keys(this.cacheKeyPrefix + "*");
    }

    @Override
    public int size() {
        Set<String> keys = redisUtils.keys(this.cacheKeyPrefix + "*");
        return keys.size();
    }

    @Override
    public Collection<V> values() {
        Set<String> keys = this.redisUtils.keys(this.cacheKeyPrefix + "*");
        if (CollectionUtils.isEmpty(keys)) {
            return Collections.emptySet();
        } else {
            List<Object> values = new ArrayList<>(keys.size());
            Iterator<String> iterator = keys.iterator();
            while (iterator.hasNext()) {
                String key = iterator.next();
                Object value = this.redisUtils.get(key);
                if (value != null) {
                    values.add(value);
                }
            }
            return (Collection<V>) values;
        }
    }
}
