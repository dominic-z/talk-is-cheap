var events = require('events');
var emitter = new events.EventEmitter();


// 如果没有这个监听，会抛出异常并中断
emitter.addListener('error', function (e) {
    console.error("error", e);
})

emitter.emit('error','aaa'); 
