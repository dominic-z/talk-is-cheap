# AGENTS.md

## 项目概述

`talk-is-cheap` 是个人技术学习与实验仓库，按主题划分目录：`AI/`、`database/`、`container/`、`distributed-system/`、`hadoop/`、`java-playground/`、`linux/`、`maven/`、`message-queue/` 等，内容以「可运行的示例代码 + 笔记」为主。

## Python 运行环境（硬性规则）

本仓库可能在不同电脑上运行，**conda 的安装目录和环境名在各机器上都不一致**，因此下面的规则以「行为约束」为准，不要依赖任何写死的路径或环境名。

### 第一原则

本仓库所有 Python 相关工作（阅读/修改代码、安装依赖、运行脚本、跑测试）**必须**在 miniconda 创建的独立环境中进行。

**严格禁止以任何方式使用、修改系统的 Python 环境。**

### 明确禁止（任何理由都不例外）

- 执行系统解释器：`/usr/bin/python`、`/usr/bin/python3`、`/usr/local/bin/python*`
- 用系统包管理器补 Python：`sudo apt install python3-*`（含 `python3-pip`、`python3-venv`、`python3-dev`）
- 向系统环境装包：`pip install`、`pip3 install`、`sudo pip`、`pip install --break-system-packages`、`pipx install`
- 用系统 Python 建 venv 后再往里装东西
- 因为「系统里没有 python / pip / 某个包」就转而安装系统包或改用系统解释器兜底
- 写入或删除系统 Python 相关路径下的内容，例如 `/usr/lib/python3*`、`/usr/local/lib/python3*`、`/etc/python*`、`~/.local/lib/python3*`

### 如何定位 conda 环境（先探测，不要写死）

```bash
# 1) 优先复用当前已激活的环境
echo "$CONDA_PREFIX"                 # 非空即说明有环境已激活

# 2) 未激活时先找 conda 本体
command -v conda || conda info --base 2>/dev/null

# 3) 仍找不到，按顺序探测常见安装位置（不同电脑位置不同，这一步是必要的）
for d in "$HOME/miniconda3" "$HOME/miniconda" "$HOME/anaconda3" \
         "$HOME/Programs/miniconda3" "$HOME/Programs/miniconda" \
         "$HOME/opt/miniconda3" "/opt/miniconda3" "/opt/conda"; do
    [ -x "$d/bin/conda" ] && echo "CONDA_BASE=$d" && break
done

# 4) 加载后列出可用环境（不要假定环境名）
source "<CONDA_BASE>/etc/profile.d/conda.sh"
conda env list
conda activate <env-name>
```

### 每次执行前必须校验

```bash
which python                                    # 或
python -c "import sys; print(sys.executable)"
```

结果必须落在 conda 环境目录内（路径中含 `conda` / `envs/`）。若指向 `/usr/bin/...` 或输出为空，**立即停止**，先修正环境，不要继续执行。

### 找不到环境时：必须向用户确认

出现以下任一情况，**停止操作并询问用户**，不要自行决定或绕过：

- 所有探测位置都没有找到 conda / miniconda
- 找到了 conda，但 `conda env list` 中没有适合当前任务的环境
- 环境存在但缺少所需依赖，且不确定该装到哪个环境

提问时说明探测结果和候选方案，例如：

> 未在本机找到 conda 环境（已探测：`~/miniconda3`、`~/Programs/miniconda3`、`/opt/miniconda3` …）。请确认 conda 的安装路径，或指定要使用的环境名；需要我新建环境吗？

**在用户确认之前，禁止**：安装 miniconda / conda、创建新环境、以及任何形式的系统 Python 安装操作。

### 依赖与运行

- 装依赖到当前 conda 环境：`python -m pip install <package>`
- 用 `python -m pip` 而不是裸 `pip`，避免 `pip` 指向别的环境
- 运行脚本：`python path/to/script.py`，或显式用 `$CONDA_PREFIX/bin/python path/to/script.py`
- 新增依赖写入对应子项目的 `requirements.txt` / `pyproject.toml`，并注明需要激活的环境

### 非交互式 shell 的坑

`~/.bashrc` 里的 `conda activate ...` 在非交互式场景（自动化脚本、部分工具调用、CI）**不会执行**，此时 `which conda` 和 `which python` 都可能为空。

正确处理：先 `source "<CONDA_BASE>/etc/profile.d/conda.sh"` 再 `conda activate <env>`；仍不可用则**向用户确认**。**禁止**因为走到这一步就退回系统 Python。

## 通用约定

- 不要自行用系统包管理器安装语言运行时（Python / Node / Java 等），除用户明确要求。
- 不要提交任何真实密钥，`.env` 之类的文件不应进入版本库；需要示例时提供 `.env.example`。
- 仓库按主题分区，改动尽量局限在当前子目录内，不要顺手重构其他主题的代码。
