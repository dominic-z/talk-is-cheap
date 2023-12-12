import React, { useEffect, useState } from "react";
import { context } from "./EE"
import EE from "./EE"

export default function Listener() {
    const [number, setNumber] = useState<number>(0)

    useEffect(() => {
        EE.addListener("emitter3",(a:number)=>{
            console.log(context);
            setNumber(a);
            
        },context);


        return ()=>{
            EE.removeListener("emitter3");
        }
    }, [])

    return (
        <div>
            {number}
        </div>
    )
}