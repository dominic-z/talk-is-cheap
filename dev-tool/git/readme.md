经常不小心提交大文件，记录一下清空大文件git提交记录的方法


```shell
git filter-branch --force --index-filter "git rm --cached --ignore-unmatch 你要删除的文件（相对项目的路径）" --prune-empty --tag-name-filter cat -- --all

git push origin --force --all


git for-each-ref --format='delete %(refname)' refs/original | git update-ref --stdin
git reflog expire --expire=now --all
git gc --prune=now

```