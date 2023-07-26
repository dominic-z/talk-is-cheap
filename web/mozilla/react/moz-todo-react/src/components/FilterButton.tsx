import React from "react";

function FilterButton(props:{buttonName:string,onClick:()=>void,isPressed:boolean}) {
  return (
    <button type="button" className="btn toggle-btn" aria-pressed={props.isPressed} 
    onClick={props.onClick}>
      <span className="visually-hidden">Show </span>
      <span>{props.buttonName} </span>
      <span className="visually-hidden"> tasks</span>
    </button>
  );
}

export default FilterButton;
