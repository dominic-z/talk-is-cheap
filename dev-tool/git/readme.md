经常不小心提交大文件，记录一下清空大文件git提交记录的方法


```shell

# You need to run this command from the toplevel of the working tree.在git的根目录来执行
git filter-branch --force --index-filter "git rm --cached --ignore-unmatch container/kubernetes/tutorials/concept/services-networking/gateway/install.yaml" --prune-empty --tag-name-filter cat -- --all

git push origin --force --all


git for-each-ref --format='delete %(refname)' refs/original | git update-ref --stdin
git reflog expire --expire=now --all
git gc --prune=now

```