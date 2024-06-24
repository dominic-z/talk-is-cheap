import React, { useEffect, useMemo, useState } from "react";
import KeyDemoInner from "./KeyDemoInner";


export default function KeyDemo2() {
    const [items, setItems] = useState(
        [
            { id: 1, text: 'Item 1',value:Math.random() },
            { id: 2, text: 'Item 2' ,value:Math.random()},
            { id: 3, text: 'Item 3' ,value:Math.random()},
          ]
    )

    let handleClick = () => {
        const newItems = [
          { id: 1, text: 'Item 1' ,value:Math.random()},
          { id: 2, text: 'Item 2 (updated)' ,value:Math.random()},
          { id: 3, text: 'Item 3' ,value:Math.random()},
        ];
        setItems(newItems);
      };
    
      console.log('render whole list')
      return (
        <div>
          <ul>
            {items.map(item => (
                <>
                <li key={item.id}>{item.text}</li>
                <KeyDemoInner key={item.id} content={item.value+''}></KeyDemoInner>
                </>
            ))}
          </ul>
          <button onClick={handleClick}>Update</button>
        </div>
      );


}