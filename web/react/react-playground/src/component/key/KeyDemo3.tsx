import React, { useEffect, useMemo, useState } from "react";
import KeyDemoInner from "./KeyDemoInner";


export default function KeyDemo3() {
  let [item, setItem] = useState("1")


  console.log('render whole list')
  // 每当item变化的时候，key就会变化，如果key变化，那么react就会认为这是一个新的组件，不能与之前共用如useState等数据，如下面的第二个KeyDemoInner所示，里面的c每次都会变更。需要额外说明，key的特性不具备记忆性质，比如说key从1->2->1，react是无法记得上次的1是什么的。react只知道从1->2要重新构建组件，从2->1也要重新构建。key仅仅是用于标记
  // 否则，就会认为不是新的组件，从而共用useState的值，如下面的第一个KeyDemoInner，里面每次c都是一开始的1
  return (
    <>
      <input value={item} onChange={e => setItem(e.target.value)}></input>
      <KeyDemoInner content={item}></KeyDemoInner>
      <KeyDemoInner key={item} content={item}></KeyDemoInner>
    </>

  );


}