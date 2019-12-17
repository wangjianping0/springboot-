# spring-boot-first-version-learn

#### 介绍
spring boot项目中克隆后，回退到了第一个版本，本工程即为那时的代码，提交时间是2013年4月。
依赖的spring 版本是spring 4.0.0.BOOTSTRAP-SNAPSHOT
本工程对代码进行了注释，方便阅读

#### 软件架构
软件架构说明


#### 使用说明
如果遇到maven依赖下载不下来，请查看是否是snapshot类型的依赖，如果是的话，可以考虑在maven的
settings.xml中添加以下部分：
  <profiles>
    <profile>
        <id>allow-snapshots</id>
        <activation>
            <activeByDefault>true</activeByDefault>
        </activation>
        <repositories>
            <repository>
                <id>spring-snapshots</id>
                <name>Spring Snapshots</name>
                <url>http://maven.springframework.org/snapshot</url>
                <snapshots>
                    <enabled>true</enabled>
                </snapshots>
            </repository>
        </repositories>
    </profile>
</profiles>

#### 参与贡献

1.  Fork 本仓库
2.  新建 Feat_xxx 分支
3.  提交代码
4.  新建 Pull Request


#### 码云特技

1.  使用 Readme\_XXX.md 来支持不同的语言，例如 Readme\_en.md, Readme\_zh.md
2.  码云官方博客 [blog.gitee.com](https://blog.gitee.com)
3.  你可以 [https://gitee.com/explore](https://gitee.com/explore) 这个地址来了解码云上的优秀开源项目
4.  [GVP](https://gitee.com/gvp) 全称是码云最有价值开源项目，是码云综合评定出的优秀开源项目
5.  码云官方提供的使用手册 [https://gitee.com/help](https://gitee.com/help)
6.  码云封面人物是一档用来展示码云会员风采的栏目 [https://gitee.com/gitee-stars/](https://gitee.com/gitee-stars/)
