
# 概述

maven-resources-plugin在 Maven 构建体系里是相当重要的插件，其主要职责是处理项目资源。以下为你详细介绍该插件，涵盖它的功能、配置、常用目标与使用场景等内容。

主要功能
1. 资源复制：把项目里的资源文件从源目录复制到目标目录。像src/main/resources目录下的文件通常会被复制到target/classes目录，src/test/resources目录下的文件会被复制到target/test-classes目录。
2. 资源过滤：在复制资源文件时，能对文件中的占位符进行替换，替换成预先定义的属性值。此功能在不同环境使用不同配置时特别有用。
3. 编码处理：可以指定资源文件在复制时的编码，保证文件编码的一致性。

说人话就是将一些文件（例如yml、properties）里的一些变量，用来自另一个文件（例如yml、properties、pom）里定义的变量替代

指定profile，然后就可以看到target里的一些参数已经变了
```shell
mvn clean compile -P prod
```