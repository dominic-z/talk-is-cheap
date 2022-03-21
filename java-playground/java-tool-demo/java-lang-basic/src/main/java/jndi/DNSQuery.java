package jndi;

/**
 * @author dominiczhu
 * @version 1.0
 * @title DNSQuery
 * @date 2021/10/20 下午9:40
 */

import java.util.Hashtable;

import javax.naming.Context;

import javax.naming.NamingEnumeration;

import javax.naming.NamingException;

import javax.naming.directory.Attribute;

import javax.naming.directory.Attributes;

import javax.naming.directory.DirContext;

import javax.naming.directory.InitialDirContext;


public class DNSQuery {

    public static void main(String[] args) throws NamingException {

/*第一个参数指定要查询的域或主机名，第二个参数指定查询的DNS服务器,

为了程序的简单易读性，省略了严格的参数错误检查*/

        String domain = args[0];

        String dnsServer = args.length < 2 ? "" : ("//" + args[1]);


//通过环境属性来指定Context的工厂类

        Hashtable env = new Hashtable();

        env.put(Context.INITIAL_CONTEXT_FACTORY,

                "com.sun.jndi.dns.DnsContextFactory");

        env.put(Context.PROVIDER_URL, "dns:" + dnsServer);

        DirContext ctx = new InitialDirContext(env);

//分别获取包含所有属性和只包含Mx属性的Attributes对象

        Attributes attrsAll = ctx.getAttributes(domain);

        Attributes attrsMx = ctx.getAttributes(domain, new String[]{"MX"});



/*上面的整段程序代码也可以用下面这段程序代码来替代，下面这段程序

代码通过查询URL中的Schema信息来自动选择Context的工厂类*/

/*

DirContext ctx = new InitialDirContext();

Attributes attrsAll = ctx.getAttributes("dns:" + dnsServer + "/" + domain);

Attributes attrsMx = ctx.getAttributes(

"dns:" + dnsServer + "/" + domain, new String[]{"MX"});

*/


        System.out.println("打印出域" + domain +

                "的Attributes对象中的信息：");

        System.out.println(attrsAll);

        System.out.println("--------------------------");

        System.out.println("打印只检索域" + domain +

                "的MX记录的Attributes对象：");

        System.out.println(attrsMx);


        System.out.println("--------------------------");

        System.out.println("逐一打印出Attributes对象中的各个属性：");

        NamingEnumeration attributes = attrsAll.getAll();

        while (attributes.hasMore()) {

            System.out.println(attributes.next());

        }


        System.out.println("--------------------------");

//直接调用get方法从attrsMx集合检索MX属性

        System.out.println("直接检索Attributes对象中的MX属性：");

        Attribute attrMx = attrsAll.get("MX");

        System.out.println(attrMx);


        System.out.println("--------------------------");

//获取Mx属性中的第一个值:

        System.out.println("获取Mx属性中的第一个值:");

        String recordMx = (String) attrMx.get();

        System.out.println(recordMx);

//从Mx属性的第一个值中提取邮件服务器地址

        System.out.println("从MX属性值中提取的邮件服务器地址：");

        String smtpServer = recordMx.substring(

                recordMx.indexOf(" ") + 1);

        System.out.println(smtpServer);

    }

}