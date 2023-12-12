import React, { useEffect, useState } from "react";
import { context } from "./EE"
import EE from "./EE"

export default function Emitter() {
    

    return (
        <div>
            <input type="button" onClick={()=>EE.emit("emitter3",123)} value="按钮"/>
        </div>
    )
}