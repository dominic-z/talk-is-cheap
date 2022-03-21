
# 使用protoc进行生成代码
`examples/addressbook.proto`是proto3的语法
```shell script
cd ProtocolBufferDemo/src/main/java
protoc -I=. --java_out=. addressbook3.proto
```
不过由于我要用插件，批量编译的话，这两个文件由于package相同而且Message的名也相同，所以mvn protobuf compile的时候会报错，因此我把proto3的版本从根目录下挪出来

`addressbook2.proto`是proto2的语法
```shell script
protoc -I=. --java_out=. examples/addressbook2.proto
```
也可以使用通配符
```shell script
protoc -I=. --java_out=. examples/*.proto
```
二者语法类似，目前发现的差异：
1. proto3的字段修饰符没有`require`，不带修饰符的字段默认为`require`，proto2和proto3都有`optional,repeated`
2. 生成的代码大差不差，不过proto2生成的代码里会多一些方法，例如`hasXXX`，对于`repeated`字段还有`getXXXCount`等方法，proto3语法生成的代码就没有这些方法

# 利用maven插件进行批量protoc
利用pom插件，`mvn protobuf:compile`生成的class文件会在target\generated class下面。目前我发现只能生成class文件，并且这些class文件可以被idea直接解析使用

~~另外，直接单独对ProtocolBufferDemo这个子model进行`mvn compile`会报错，即使我已经`mvn protobuf:compile`过，说程序包com.Example.tutorial.AddressBookProtos不存在，这也就是说直接打包子model的话，还是需要真实存在的java文件才行。~~

更新：这样是可以的

但如果对父项目JavaToolDemo进行整体`mvn compile`，就能成功地对所有子项目编译成功。proto的文件也能成功生成class代码并放入target\classes

所以目前来看，如果要打包的话，只能通过对父项目进行整体打包才能顺利地生成proto对应的code。