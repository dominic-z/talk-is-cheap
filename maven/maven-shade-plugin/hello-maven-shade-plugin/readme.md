# maven-shade-plugin打uber-jar

就是将依赖包里的代码打包进本工程输出的jar里。可以用解压软件解压jar，看输出的class里多了配置的依赖的class，
并且会输出一个dependency-reduced-pom.xml，如果这个jar包被install了，这个jar包在maven仓库里的坐标pom就是这个dependency-reduced-pom.xml

## artifacts设置
- include的jar会被解析并从中抽取class文件打包到uber-jar里，并且install时生成的pom的dependency里不会出现该include的jar
- exclude的jar会不会被解析，install时生成的pom的dependency里会出现该include的jar

## filters设置
与上面类似，只不过指令不同而已

## 尝试依赖一个uber-jar
uber-jar是可以install或者deploy的，但是有设置了`shadedArtifactAttached`，那么本地仓库会有两个jar包，一个是shaded后的uber-jar，一个是未shaded的；


# maven-shade-plugin解决jar-conflict