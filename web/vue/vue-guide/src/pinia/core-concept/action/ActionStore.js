import { computed, ref } from "vue";
import { defineStore } from "pinia";


export const useActionStore = defineStore('actionStore', () => {
    let count = ref(1)
    let msg = ref('')
    function increament() {
        count.value++;
    }

    function randomizeCounter() {
        count.value = Math.round(100 * Math.random())
    }

    async function registry(loginname) {
        
        try {
            const taskResult = await createTimeConsumingTask(2000, loginname + '注册任务');
            console.log('任务成功：', taskResult);
            msg.value = "成功：" + taskResult.result
            return 'success' 

        } catch (error) {
            console.log('任务失败：', error);
            msg.value = '任务失败：' + error.message
            return 'fail'
        }

    }

    return {count,msg, increament, randomizeCounter,registry}

})



// 创建一个返回Promise的耗时任务函数
function createTimeConsumingTask(delayMs, taskName = '任务') {
    // 返回一个Promise对象
    return new Promise((resolve, reject) => {
        console.log(`${taskName}开始执行，预计耗时${delayMs}毫秒`);

        // 使用setTimeout模拟耗时操作
        setTimeout(() => {
            // 随机决定任务成功或失败，增加真实性
            const isSuccess = Math.random() > 0.2; // 80%成功率

            if (isSuccess) {
                // 任务成功，返回结果
                resolve({
                    taskName,
                    status: '完成',
                    duration: delayMs,
                    result: `这是${taskName}的处理结果`
                });
            } else {
                // 任务失败，返回错误信息
                reject(new Error(`${taskName}执行失败：处理过程中发生错误`));
            }
        }, delayMs);
    });
}

// 使用示例
async function runTasks() {
    try {
        // 执行一个耗时2秒的任务
        const result1 = await createTimeConsumingTask(2000, '数据处理任务');
        console.log('任务结果：', result1);

        // 再执行一个耗时1.5秒的任务
        const result2 = await createTimeConsumingTask(1500, '文件生成任务');
        console.log('任务结果：', result2);

    } catch (error) {
        // 捕获并处理任务中可能出现的错误
        console.error('任务执行出错：', error.message);
    }
}




