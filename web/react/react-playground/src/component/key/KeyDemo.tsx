import React, { useEffect, useMemo, useState } from "react";
import KeyDemoInner from "./KeyDemoInner";


export default function KeyDemo() {
    const [arr, setArr] = useState(
        Array(5).fill("数字是：").map((value, index) => ({ id: index, content: value + index }))
    )

    function onChange(e: React.ChangeEvent<HTMLInputElement>, idx: number) {
        setArr(
            arr.map((value, index) => {
                if (index === idx) {
                    return {
                        ...value,
                        content: e.target.value
                    }
                }
                return value;
            })
        )
    }

    function onDelete(e: React.MouseEvent<HTMLInputElement>, idx: number) {
        setArr(arr.filter((value, index) => {
            if (idx === index) {
                return false;
            }
            return true;
        }))
    }

    // 使用useMemo防止修改普通输入框的时候重新渲染inputList
    const inputList = useMemo(() => {
        console.log('rerender inputlist')
        return arr.map((value, index) => {
            return <div key={index}>
                <span>序号：{index}</span>
                <span>非可控输入框：
                    <input defaultValue={value.content} onChange={e => onChange(e, index)} />
                </span>
                <span>可控输入框：
                    <input value={value.content} onChange={e => onChange(e, index)} />
                </span>
                <input type={"button"} onClick={e => onDelete(e, index)} value='delete' />

                <KeyDemoInner content={value.content}></KeyDemoInner>
            </div>
        })

    }, [arr])

    let [v, setV] = useState('')

    return <div>
        <div>普通的输入框（value固定）：
            <input defaultValue={213} value={v} onChange={e => setV(e.target.value)} />
            普通的输入框（value不指定）：
            <input defaultValue={213} onChange={e => e.target.value = '就不改！'} />
        </div>
        {inputList}

    </div>


}