# 项目说明

这是一个学习[superpowers-zh](https://github.com/jnMetaCode/superpowers-zh)的demo，我用的是qoder。

superpowers是提炼出来了一套标准的开发流程，并将每个开发流程设计成一个标准的小环节。

# 安装
```shell
# 在当前工程下
npx superpowers-zh --tool qoder

# 随后会自动在.qoder/skills和.qoder/rules安装skill，但是我并不想将这些东西传到我的仓库，因此我用了符号链接。。
mkdir git_ignore

mv .qoder/rules git_ignore/rules 
ln -s $(pwd)/git_ignore/rules .qoder/rules
mv .qoder/skills git_ignore/skills
ln -s $(pwd)/git_ignore/skills .qoder/skills
```

# project-travel-mouse

> /brainstorming @​01-task.md​ 阅读这个需求文档，并进行brainstorming

