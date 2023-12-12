import EventEmitter from "events";
import React, { useEffect, useRef, useState } from "react";
import eventBus from "./EventBus";


interface Student {
    name: string
    age: number
}
export default function EventAHandler() {

    const [s, setS] = useState<Student>({ name: "小明", age: 22 });


    // 执行结果
    // 卸载  解释：严格模式，开发者模式会模拟一次卸载组件，因此触发了卸载
    // handle Sevent   解释
    // tome 12
    // handle Sevent
    // tome 12
    // 卸载
    function eventAFunction(name: string, age: number) {

        console.log("handle Sevent")
        console.log(name, age)
        setS({ name: name, age: age })
    }

    useEffect(() => {
        eventBus.addListener("event-a", eventAFunction);

        function componentWillUnmount() {
            // 组件销毁时你要执行的代码，卸载时需要把事件注册解除，这是个好习惯
            console.log("卸载",eventBus.listeners("event-a"))
            eventBus.removeListener("event-a", eventAFunction);
        }

        return componentWillUnmount;
    }, [])

    console.log(eventBus.listeners("event-a"));

    // 也可以在外面添加监听器，但因为可能会触发setState，因此如果不判断的话，可能会有多个"event-a"的处理器被注册。
    // if (eventBus.listeners("event-a").length == 0) {
    //     eventBus.addListener("event-a", eventAFunction);
    // }
   
    return (
        <div>{s.name}</div>
    )
}




