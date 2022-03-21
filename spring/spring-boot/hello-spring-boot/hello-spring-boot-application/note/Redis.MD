
# 序列化器
大踩坑 大踩坑

测试代码见`spring-boot-composition/src/test/java/com/example/springboot/hellospringboot/RedisTest.java#testSerializer`
针对lettuce，一般常用的类是`StringRedisTemplate`，他将操作的kv都当做String类型，put进去什么样，就是什么样；例如，如果执行下列代码
```java
final ListOperations<String, String> ops = stringRedisTemplate.opsForList();
final String listKey = "myList";
stringRedisTemplate.delete(listKey);
ops.rightPush(listKey,"1");
ops.rightPush(listKey,"2");
ops.rightPush(listKey,"n3");
ops.rightPush(listKey,"一只小猪");
final List<String> myList = ops.range(listKey, 0, -1);
log.info("myList:{}", myList);
```
那么从redis里得到的结果就是
```shell
127.0.0.1:6379> lrange myList 0 -1
1) "1"
2) "2"
3) "n3"
4) "\xe4\xb8\x80\xe5\x8f\xaa\xe5\xb0\x8f\xe7\x8c\xaa"
```

但还有另一些需要定制化的`RedisTemplate<K,V>`类型
```java
RedisTemplate<String, Long> longDefaultSerializerTemplate = new RedisTemplate<>();
        longDefaultSerializerTemplate.setConnectionFactory(redisConnectionFactory);
        longDefaultSerializerTemplate.afterPropertiesSet();
final ValueOperations<String, Long> longDefaultSerializerOps = longDefaultSerializerTemplate.opsForValue();
        longDefaultSerializerOps.set("long-default", 1L);
        log.info("{}", longDefaultSerializerOps.get("long-default"));
```
得到结果
```cmd
2022-01-16 14:46:36.189 [INFO ] 58704 --- [main] RedisTest  : 1
```

看起来好像没什么问题，但此时看redis，可以发现，找不到一个叫做`long-default`的key，取而代之的是一坨带前缀的奇怪字符，并且get这个key，得到的也是一坨奇怪的东西

```shell
127.0.0.1:6379> keys *
1) "\xac\xed\x00\x05t\x00\x0clong-default"
127.0.0.1:6379> get "\xac\xed\x00\x05t\x00\x0clong-default"
"\xac\xed\x00\x05sr\x00\x0ejava.lang.Long;\x8b\xe4\x90\xcc\x8f#\xdf\x02\x00\x01J\x00\x05valuexr\x00\x10java.lang.Number\x86\xac\x95\x1d\x0b\x94\xe0\x8b\x02\x00\x00xp\x00\x00\x00\x00\x00\x00\x00\x01"
```

之所以出现这种情况，是因为`RedisTemplate`在与redis交互过程中，对于任何一个set调用（将之抽象为set(k,v)），`RedisTemplate`会有几个我们看不见的操作；
1. `RedisTemplate`将k进行序列化，得到`realK = encode(k)`；
2. `RedisTemplate`将v进行序列化，得到`realV = encode(v)`；
3. `RedisTemplate`将`realK,realV`送给redis，作为redis内真正的键值对，存储进redis里；

因此，在redis内部会有上述那些奇怪的乱码；同样地，对于get操作`v=get(k)`，`RedisTemplate`也会有几个我们看不见的操作；
1. `RedisTemplate`将k进行序列化，得到`realK = encode(k)`；
2. `RedisTemplate`将`realK`传递给redis，redis使用`realK`进行查询，得到`realV`，回传给`RedisTemplate`；
3. `RedisTemplate`将`realV`反序列化，得到`v = decode(realV)`，并赋值为调用的v；

`RedisTemplate`默认使用jdk的序列化器，因此，会输出一坨坨乱码，并且，redis自始至终看到的都是`realK,realV`

但如果错误指定了序列化器，也会报错，如下所示，因为value是long类型，`StringRedisSerializer`无法处理long类型，因此报错了。
```java
// 使用String序列化器
        RedisTemplate<String, Long> longStringSerializerTemplate = new RedisTemplate<>();
        longStringSerializerTemplate.setConnectionFactory(redisConnectionFactory);
        longStringSerializerTemplate.setKeySerializer(new StringRedisSerializer());
        longStringSerializerTemplate.setValueSerializer(new StringRedisSerializer());
        longStringSerializerTemplate.afterPropertiesSet();
        final ValueOperations<String, Long> longStringSerializerOps = longStringSerializerTemplate.opsForValue();
        try {
            longStringSerializerOps.set("long-string", 1L);
            log.info("{}", longStringSerializerOps.get("long-string"));
        } catch (Exception e) {
            log.error("Long cannot be encode or decode by StringRedisSerializer", e);
        }
```

下面的做法就比较好了，设定了key和value的序列化器

```java
        // 使用StringRedisSerializer做key的序列化器，因为key一般都是string
        // 使用Jackson做value的序列化器
        RedisTemplate<String, Long> longGenericJacksonTemplate = new RedisTemplate<>();
        longGenericJacksonTemplate.setConnectionFactory(redisConnectionFactory);
        longGenericJacksonTemplate.setKeySerializer(new StringRedisSerializer());
        longGenericJacksonTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        longGenericJacksonTemplate.afterPropertiesSet();
        final ValueOperations<String, Long> longGenericJacksonOps = longGenericJacksonTemplate.opsForValue();
        final String longJacksonKey = "long-jackson";
        longGenericJacksonOps.set(longJacksonKey, 1L);
        log.info("{} {}", longJacksonKey, longStringSerializerOps.get(longJacksonKey));

        // 使用StringRedisSerializer做key的序列化器，因为key一般都是string
        // 使用Jackson做value的序列化器
        RedisTemplate<String, DemoObj> objectGenericJacksonTemplate = new RedisTemplate<>();
        objectGenericJacksonTemplate.setConnectionFactory(redisConnectionFactory);
        objectGenericJacksonTemplate.setKeySerializer(new StringRedisSerializer());
        objectGenericJacksonTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
//        也可以指定类型
//        objectGenericJacksonTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<>(DemoObj.class));
        objectGenericJacksonTemplate.afterPropertiesSet();
        final ValueOperations<String, DemoObj> obgGenericJacksonOps = objectGenericJacksonTemplate.opsForValue();
        final String objJacksonKey = "obj-jackson";
        obgGenericJacksonOps.set(objJacksonKey, DemoObj.getDemoObj());
        log.info("{} {}", objJacksonKey, obgGenericJacksonOps.get(objJacksonKey));
```


不过看了这么多，其实一个无脑且有效的方法是，就用string，编码和解码操作都我们代码里手动搞，这样清晰又明确


## lua脚本
如果我们用`RedisTemplate`执行lua脚本，`RedisTemplate`会对我们设置的key和value一样进行编码解码的操作。这就有可能导致异常。
例如，如果我们使用了默认的序列化器，然后执行了下列脚本
```java
String testLua = "return tonumber(ARGV[1]);";
        RedisScript<Long> defaultRedisScript = new DefaultRedisScript<>(testLua, Long.class);
        final Long res = longDefaultSerializerTemplate.execute(defaultRedisScript, Arrays.asList("s"), 1);
        log.info("{}", res);
```
得到的结果为null，这是因为真正送给redis的key为`realK`，他就不是一个数字，是一坨jdk序列化的字符串，并且看源码如下，
得知对参数与结果默认使用的是value的序列化器，正巧对于lua来说，执行`tonumber(13ba)`这种操作，返回值就是nil，所以，返回值为null
```java
public <T> T execute(final RedisScript<T> script, final List<K> keys, final Object... args) {
		// use the Template's value serializer for args and result
		return execute(script, template.getValueSerializer(), (RedisSerializer<T>) template.getValueSerializer(), keys,
				args);
	}

	public <T> T execute(final RedisScript<T> script, final RedisSerializer<?> argsSerializer,
			final RedisSerializer<T> resultSerializer, final List<K> keys, final Object... args) 
```

# 缓存

## spring内置的内存缓存
涉及的注解有`@Cacheable`/`@CacheConfig`/并且在启动类里加上`@EnableCaching`