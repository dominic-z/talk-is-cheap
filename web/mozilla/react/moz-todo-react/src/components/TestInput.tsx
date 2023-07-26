import React, { useEffect } from "react";

interface Props{
    name:string
}

export default function TestInput(props: Props) {
    console.log("abc");

    useEffect(()=>{
        console.log("reander in effect")
    })
    return (
        <input value={props.name}></input>
    )
}
