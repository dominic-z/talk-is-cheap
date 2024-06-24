
import React from "react";


interface Props{
    num:number,
    s:string
}
export default function C2(props:Props){
    return <div>
        <p>{props.num}</p>
        <input type="text" />
    </div>


}