import React from "react";
import "./css/TransitionBlock.css"
export default function TransitionBlock(){

    return (
        <div className="box" onClick={e=>{
            const currentClassList = e.currentTarget.classList
            
            if (currentClassList.contains("clicked-box")){
                e.currentTarget.classList.remove("clicked-box")
                e.currentTarget.classList.add("box")
            }else{
                e.currentTarget.classList.remove("box")
                e.currentTarget.classList.add("clicked-box")
            }
        }}>
            example
        </div>
    )

}