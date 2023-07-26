
import React, { useEffect, useRef } from "react";
export default function usePrevious<T>(value: T) {
    const ref = useRef<T>();
    console.log(Object.getPrototypeOf(ref));
    useEffect(() => {
        ref.current = value;
    });
    return ref.current;
}