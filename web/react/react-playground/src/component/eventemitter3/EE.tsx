import EventEmitter from "eventemitter3";


const EE = new EventEmitter();

export default EE;
export const context = {foo:"bar"};