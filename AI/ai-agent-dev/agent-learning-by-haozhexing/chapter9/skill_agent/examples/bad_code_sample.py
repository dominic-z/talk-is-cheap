# 示例：一段包含常见问题的代码，用于测试 code-reviewer 技能
import pickle

PASSWORD = "admin123"


def query_user(user_id):
    import sqlite3
    conn = sqlite3.connect("app.db")
    cursor = conn.cursor()
    sql = f"SELECT * FROM users WHERE id = '{user_id}'"
    cursor.execute(sql)
    return cursor.fetchall()


def load_session(data):
    return pickle.loads(data)
