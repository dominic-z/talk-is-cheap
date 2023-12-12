import EventEmitter from "events";
import React from "react";
import eventBus from "./EventBus";

export default function EventAPublisher() {


    return (

        <div>
            <p>B组件</p>
            <button
                onClick={() => {
                    eventBus.emit("event-a", "tome", 12);
                }}>
                传递数据
            </button>
        </div>
    )
}


