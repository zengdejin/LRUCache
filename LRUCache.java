
import java.util.*;

/**
 * 基于LinkedHashMap，实现LRU且有时效性功能的HashMap
 * @author Ted
 * @time 2017/08/16
 */
public class LRUCache<K, V> {

    private static final float hashTableLoadFactor = 0.75f;

    private LinkedHashMap<K, V> map;
    //timeExpireMap 管理key-value的过期时间
    private LinkedHashMap<K,Long> timeExpireMap;
    private int cacheSize;

    private long expireTime = 60 * 60 * 1000;//默认过期时间一小时

    /**
     * @param cacheSize 缓存最大值
     * @param expireTime 过期时长，单位毫秒
     */
    public LRUCache(int cacheSize , long expireTime){
        this(cacheSize);
        this.expireTime = expireTime;

    }
    /**
     * @param cacheSize 缓存最大值
     */
    public LRUCache(int cacheSize) {
        this.cacheSize = cacheSize;
        int hashTableCapacity = (int) Math.ceil(cacheSize / hashTableLoadFactor) + 1;
        map = new LinkedHashMap<K, V>(hashTableCapacity, hashTableLoadFactor, true) {
            // (an anonymous inner class)
            private static final long serialVersionUID = 1;

            //重写是否删除最后元素的逻辑，保证联表只有初始化的大小。删除逻辑在linkedHashMap中
            @Override
            protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
                return size() > LRUCache.this.cacheSize;
            }
        };
        timeExpireMap = new LinkedHashMap<K, Long>(hashTableCapacity, hashTableLoadFactor, true) {
            // (an anonymous inner class)
            private static final long serialVersionUID = -1;

            //重写是否删除最后元素的逻辑，保证联表只有初始化的大小。删除逻辑在linkedHashMap中
            @Override
            protected boolean removeEldestEntry(Map.Entry<K, Long> eldest) {
                return size() > LRUCache.this.cacheSize;
            }
        };
    }

    /**
     * 根据key 获取map的值，没找到则返回null
     */
    public V get(K key) {
        V v = map.get(key);
        if(v!=null){
            Long putTime =  timeExpireMap.get(key);
            //超时则返回空
            //timeExpireMap 管理key-value的过期时间，key对应的过期时间过期了则返回null
            if((putTime == null) || (putTime!=null&&System.currentTimeMillis()-putTime >= this.expireTime)){
                return null;
            }
        }
        return v;
    }

    /**
     * 根据key 把值put到map中，需要保证同步
     */
    public synchronized void put(K key, V value) {
        map.put(key, value);
        //timeExpireMap 管理key-value的过期时间 ， 值为当前时间毫秒数
        timeExpireMap.put(key , System.currentTimeMillis());
    }

    /**
     * 清空缓存
     */
    public synchronized void clear() {
        map.clear();
        timeExpireMap.clear();
    }

    /**
     * 返回当前缓存的大小
     */
    public synchronized int usedEntries() {
        return map.size();
    }

    /**
     * 返回集合
     */
    public synchronized Collection<Map.Entry<K, V>> getAll() {
        return new ArrayList<Map.Entry<K, V>>(map.entrySet());
    }
}
