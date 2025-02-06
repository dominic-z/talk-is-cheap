此文为阿里oss的练习相关代码

[阿里oss文档](https://help.aliyun.com/zh/oss/product-overview/what-is-oss)写的相当详细了。

# 产品概述

## 产品简介

### 什么是对象存储OSS

accessKey这个概念后续会总用到
AccessKey简称AK，指的是访问身份验证中用到的AccessKey ID和AccessKey Secret。OSS通过使用AccessKey ID和AccessKey Secret对称加密的方法来验证某个请求的发送者身份。AccessKey ID用于标识用户；AccessKey Secret是用户用于加密签名字符串和OSS用来验证签名字符串的密钥，必须保密。对于OSS来说，AccessKey的来源有：

- Bucket的拥有者申请的AccessKey。
- 被Bucket的拥有者通过RAM授权给第三方请求者的AccessKey。
- 被Bucket的拥有者通过STS授权给第三方请求者的AccessKey

至于sts是啥
> 您可以通过STS服务为其他用户颁发一个临时访问凭证，用户通过临时访问凭证可以在限定的有效期内，以符合策略规定的权限访问OSS资源。超过有效期后，该凭证自动失效，无法继续通过该凭证访问OSS资源，确保了访问控制的灵活性和时效性。

## 产品计费

计费：费用有很多种，主要的是三种：存储费用、流量费用、请求费用。按量计费（用多少交多少）、资源包（先买后用，例如买一个“标准 - 同城冗余（ ZRS ）存储包”，在使用“标准 - 同城冗余（ ZRS ）”存储的时候就可以抵扣）、预留空间（针对存储费用的付费包）

# 快速入门

## 开始使用oss
先开通再说，在oss的产品首页有“立即开通”、“立即购买”，开通不要钱，因为默认按量计费，也可以点立即购买，买资源包，学习为主，二话不说，直接“立即开通”，不要钱。



## 控制台快速入门
开通后就可以在[控制台](https://oss.console.aliyun.com/overview)操作了，在根据教程创建一个账号管理员，用这个管理员来代替主账号进行后续的操作。

### 创建存储空间
在创建存储空间（Bucket）的时候，就可以看到让我们选是标准存储还是低频存储，是本地冗余还是同城冗余。bucket直译就是水桶、筐，相当于划分了一块独立的存储空间用来存储一样。

1. 地域名称：其实没所谓，有地域属性就会让你选云服务实际架设在哪，同一地区的云服务之间可以通过内网进行访问。无地域属性就无法做到，这里可以选有地域属性，然后选个距离自己近的城市

2. 存储类型与冗余类型：这里可以选个便宜的（价格可以参照https://www.aliyun.com/product/oss中的资源包价格。）。这里选择标准存储、本地冗余，这个6个月40g只要9块钱。

3. bucket名称：随便写,但是需要全局唯一，全阿里云唯一，起个响亮的名字。dz-20250203-official-guide-demo1

### 分享
分享链接默认有效时长是300s，可以自行设置。超时之后再尝试下载就会返回下面的xml
```xml
<Error>
<Code>AccessDenied</Code>
<Message>Request has expired.</Message>
<RequestId>67A0301DFD9B3D32381FFE63</RequestId>
<HostId>dz-20250203-official-guide-demo1.oss-cn-heyuan.aliyuncs.com</HostId>
<Expires>2025-02-03T02:53:25.000Z</Expires>
<ServerTime>2025-02-03T02:55:25.000Z</ServerTime>
<EC>0002-00000069</EC>
<RecommendDoc>https://api.aliyun.com/troubleshoot?q=0002-00000069</RecommendDoc>
</Error>
```

## 命令行工具ossutil快速入门

### 安装ossutil

直接将ossutil的路径配置在环境变量里也行，然后执行`source ~/.bashrc`
```
OSSUTIL_PATH="/home/dominiczhu/Programs/ossutil-2.0.6-beta.01091200-linux-amd64"
export PATH=$OSSUTIL_PATH:$PATH
```

### 配置ossutil
通过`ossutil config`进行配置，再次输入这个命令就是重新进行配置了，可以进行修改accessID。

### 运行示例

```shell
ossutil mb oss://dz-20250203-official-guide-ossutil-demo2
```
随后可以在oss-console里看到这个新的bucket。创建可以指定各种参数，可以通过`ossutil mb -h`来查询手册。


```shell
echo 'Hello, oss!' > uploadFile.txt
ossutil cp uploadFile.txt oss://dz-20250203-official-guide-ossutil-demo2
ossutil cp oss://dz-20250203-official-guide-ossutil-demo2/uploadFile.txt uploadFile.txt.1
ossutil ls oss://dz-20250203-official-guide-ossutil-demo2
ossutil rm oss://dz-20250203-official-guide-ossutil-demo2/uploadFile.txt
```

删除bucket
```shell
ossutil rb oss://dz-20250203-official-guide-ossutil-demo2
```

## ossbrowser2

```shell
echo 'hello world' > hello-world.txt
echo 'hello oss' > hello-oss.txt
ossutil cp hello-world.txt oss://dz-20250203-official-guide-ossutil-demo2/folder1/
ossutil cp hello-oss.txt oss://dz-20250203-official-guide-ossutil-demo2/folder1/folder11/
```

增删改查没啥好讲的，这个分享可能有点不一样
### 主账号进行用户分享
相当于新建了一个ram用户，然后将这个文件内容的一些权限授权给这个ram用户。
1. 主账号登录后可以分享（授权），分享时会新建一个子用户以及授权码。这个子账户可以在ram的控制台中的用户中看到。
2. 使用这个子账户的ak是无法登录的，登录会提示无法listbucket，需要通过授权码登录，这个用户的权限也可以在ram控制台中的权限管理中看到。例如listObject、可以访问的文件夹路径等等。

实测主账号授权的时候，用阿里云账号密码或者扫码直接登录没法授权，会报错403, The akproxy is not allowed to do action:CreatePolicy，也问了阿里的ai
> 授权文件夹是否必须通过AK的方式登录？答：AccessKey（AK）登录、STS临时授权登录、授权码登录

### 子账号进行角色分享

首先要知道什么是角色，参考[RAM角色概览](https://help.aliyun.com/zh/ram/user-guide/ram-role-overview)，通俗的解释，角色是一个拥有一些权限的虚拟人（就像电影里的有独特性格经历的“角色”一样，这是个虚拟人），谁拥有这个人设，就拥有了这个角色的一些权限，比如说我现在构建了一个“角色”，这个角色可以查看一些文件，然后就可以找一个“可信实体”来扮演这个角色（就像电影剧本里的“角色”一样，有角色的时候不一定有扮演这个角色的人，需要找一个真实的人来扮演这个角色）。

子账号对某个文件/文件夹进行分享的时候，只能做“角色分享”，也就是将这个文件/文件夹的一些权限分享给这个“角色”，所以得就必须要先一个角色，并且这个角色还得有访问分享路径的权限（我发现阿里并不会默认给出权限），以要分享的路径为：`dz-20250203-official-guide-ossutil-demo2/folder1`为例，并且分享的路径为只读权限为例。具体操作如下：

1. 创建权限策略：权限策略就是一组权限的集合，现在需要创建一个针对对应路径的只读权限，在ram控制台-权限管理-权限策略-创建权限策略,直接使用脚本编辑，然后确定创建，名称随便，假设叫A

```json
{
  "Version": "1",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": "oss:GetBucketInfo",
      "Resource": "acs:oss:*:*:dz-20250203-official-guide-ossutil-demo2"
    },
    {
      "Effect": "Allow",
      "Action": "oss:ListObjects",
      "Resource": "acs:oss:*:*:dz-20250203-official-guide-ossutil-demo2",
      "Condition": {
        "StringLike": {
          "oss:Prefix": "folder1/*"
        }
      }
    },
    {
      "Effect": "Allow",
      "Action": [
        "oss:Get*",
        "oss:List*"
      ],
      "Resource": "acs:oss:*:*:dz-20250203-official-guide-ossutil-demo2/folder1/*"
    }
  ]
}
```
2. 创建一个新的角色：在ram控制台->角色中新建一个角色，假设叫`B`，创建成功后，同样在ram控制台->角色新增授权，在权限策略里选择自定义策略，给添加进去，这样这个角色B就拥有了对要分享的文件夹的权限了。
3. 然后参照视频，用子账号进行分享，角色选择`B`，然后得到一个授权码
4. 用上一步得到的授权码进行登录，即可操作了。

## 使用ossfs将OSS的Bucket挂载到Linux系统中

用不着，略

## SDK快速入门

按照教程，将OSS_ACCESS_KEY_ID和OSS_ACCESS_KEY_SECRET配置在~/.bashrc里面，然后执行demo代码，oss的sdk会自动读取当前的环境变量从而拿到accessKeyId和accessKeySecret，但是当我在ubuntu的idea里通过idea里run这个程序的时候，发现报错
> Exception in thread "main" com.aliyun.oss.common.auth.InvalidCredentialsException: Access key id should not be null or empty.
 at com.aliyun.oss.common.auth.EnvironmentVariableCredentialsProvider.getCredentials(EnvironmentVariableCredentialsProvider.java:44)


查看堆栈发现报错是如下代码，也就是说`System.getenv(AuthUtils.ACCESS_KEY_ENV_VAR)`没有拿到写在`.bashrc`里的环境变量。

```java
String accessKeyId = StringUtils.trim(System.getenv(AuthUtils.ACCESS_KEY_ENV_VAR));
String secretAccessKey = StringUtils.trim(System.getenv(AuthUtils.SECRET_KEY_ENV_VAR));
String sessionToken = StringUtils.trim(System.getenv(AuthUtils.SESSION_TOKEN_ENV_VAR));

if (accessKeyId == null || accessKeyId.equals("")) {
    throw new InvalidCredentialsException("Access key id should not be null or empty.");
}
```

但同时，如果创建下列java代码`Test.java`，并且将在终端，不通过idea，手动通过`javac Test.java`和`java Test`运行，发现还是能拿到这两个环境变量的。
```java
import java.util.Iterator;
import java.util.Map;
public class Test {
    public static  void readEnv(){
        System.out.println("OSS_ACCESS_KEY_ID="+System.getenv("OSS_ACCESS_KEY_ID"));
        System.out.println("OSS_ACCESS_KEY_SECRET="+System.getenv("OSS_ACCESS_KEY_SECRET"));

        Map<String, String> map = System.getenv();
        Iterator it = map.entrySet().iterator();
        while(it.hasNext())
        {
            Map.Entry entry = (Map.Entry)it.next();
            System.out.print(entry.getKey()+"=");
            System.out.println(entry.getValue());
        }
    }
    public static void main(String[] args) {
        readEnv();
    }
}

```

那么问题的原因就清晰了，通过idea的运行无法获取全部的环境变量，经过排查，发现idea的运行只能获取到系统级别的环境变量，具体可以在`run configuration`里`environment variables`里，点击`environment variable`右侧的小图标，在新的对话框里，下半部分就是idea能获取到的全部系统环境变量。发现里面并没有.bashrc的内容。

解决方案就是不依赖系统的环境变量了。而是直接在`run configuration`里`environment variables`里配置`OSS_ACCESS_KEY_ID`和`OSS_ACCESS_KEY_SECRET`就行。生产环境都是直接运行jar包，不会有idea里运行的问题。

