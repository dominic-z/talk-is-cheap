import React, { useState } from "react";



export default function KeyDemoInner({ content: content }: { content: string }) {

    let [c, setC] = useState(content)
    
    console.log("controlled render. content: ", content, "c: ", c)

    return <>
        <span style={{ marginRight: 10 }}>content: {content}. c: {c}</span>
    </>

}