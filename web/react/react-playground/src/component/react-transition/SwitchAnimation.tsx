import React, { useState } from "react";
import { SwitchTransition, CSSTransition } from "react-transition-group";
import "./css/SwitchAnimation.css"
export default function SwitchAnimation() {

    const [on,setOn] = useState<boolean>(true);

    return (
        <SwitchTransition mode="out-in">
            <CSSTransition classNames="btn" timeout={500} key={on?"on":"off"}>
                <button onClick={()=>setOn(!on)}>
                    {on?"on":"off"}
                </button>
            </CSSTransition>
        </SwitchTransition>
    )

}