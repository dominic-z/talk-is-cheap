import math, time

# 没运行，只是表示一下这个记忆的衰减，这个东西有点像redis
# 记忆类型及其衰减速率：身份信息永不衰减，琐碎信息快速衰减
DECAY_RATES = {
    "identity": 0.0,    # 永不衰减
    "preference": 0.01, # 缓慢衰减
    "fact": 0.05,       # 中速衰减
    "context": 0.1,     # 快速衰减
    "trivial": 0.3,     # 极快衰减
}

class MemoryWithDecay:
    """带衰减机制 + 访问增强的记忆系统。"""

    def __init__(self):
        self.memories: list[dict] = []  # 每条含 content/type/importance/created_at/access_count

    def add(self, content: str, type: str, importance: float = 0.5):
        self.memories.append({
            "content": content, "type": type, "importance": importance,
            "created_at": time.time(), "access_count": 0
        })

    def retrieve(self, query: str, top_k: int = 5) -> list[dict]:
        """综合考虑：相关性、时间衰减、访问增强。"""
        scored = []
        for mem in self.memories:
            relevance = self._compute_relevance(query, mem["content"])
            # 时间衰减：越久远的记忆强度越低
            age_hours = (time.time() - mem["created_at"]) / 3600
            decay = math.exp(-DECAY_RATES.get(mem["type"], 0.05) * age_hours)
            # 访问增强：经常被检索的记忆不容易遗忘
            access_bonus = min(0.2, mem["access_count"] * 0.02)
            # 综合评分
            score = relevance * 0.4 + mem["importance"] * decay * 0.4 + access_bonus * 0.2
            scored.append((score, mem))

        scored.sort(key=lambda x: x[0], reverse=True)
        results = []
        for score, mem in scored[:top_k]:
            mem["access_count"] += 1   # 访问计数+1（强化记忆）
            results.append({
                "content": mem["content"], "score": score,
                "type": mem["type"], "age_hours": (time.time() - mem["created_at"]) / 3600
            })
        return results

    def cleanup(self, threshold: float = 0.01) -> str:
        """清理衰减到阈值的记忆。"""
        before = len(self.memories)
        self.memories = [m for m in self.memories if self._current_strength(m) > threshold]
        return f"已清理 {before - len(self.memories)} 条衰减记忆，剩余 {len(self.memories)} 条"
