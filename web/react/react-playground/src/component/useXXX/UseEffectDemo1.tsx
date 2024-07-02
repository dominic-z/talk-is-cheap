import { useEffect, useState } from "react";


// 样例来自于https://react.dev/learn/you-might-not-need-an-effect#fetching-data
function getSomeData(s: number) {
    // 模拟了一个获取数据的延时操作，延时从1秒左右或者3秒左右
    let timeout = Math.random() > 0.5 ? (Math.random() + 2.5) * 1000 : (Math.random() + 0.5) * 1000;
    console.log(`检索数据： ${s}，延时时间：${timeout}`)
    return new Promise(resolve => setTimeout(resolve, timeout)).then(() => { return s });
    // return result;
}

export default function UseEffectDemo1() {

    let [page, setPage] = useState(0)
    console.log(page)

    // 用于承载请求返回数据，必须使用useState，一方面用于存储数据，另一方面用于获取结果后渲染当前组件
    let [someData, setSomeData] = useState(0)

    useEffect(() => {
        let ignore = false;
        getSomeData(page).then((res) => {
            console.log(`res:${res}，ignore:${ignore}`)
            if (!ignore) {
                // 此处不能简单写someData = res，因为这样的话这个只是回调函数，没法回过头写主函数空间的变量
                setSomeData(res)
            }
        })

        // useEffect的返回函数是一个cleanup函数，每次Effect重新执行前，都会调用一次这个函数
        // 也因为这个cleanup函数的存在，导致每次显示的值就是最后一次点击的数。
        // 把下面这行注释掉，你就可以看到显示的值就一直是最后返回的，而不是最后一次点击的数
        return () => {
            console.log(`cleanup:${page}`)
            ignore = true
        }
    }, [page])

    return <div>
        <input type={'button'} onClick={() => { setPage(page + 1) }} value={"下一页"} />
        <div style={{ 'fontSize': 'x-large' }}>数据为：{someData}</div>
    </div>
}