//event.js 文件
var EventEmitter = require('events').EventEmitter; 
var emitter = new EventEmitter(); 
emitter.on('some_event', function() { 
        console.log('some_event 事件触发'); 
}); 
setTimeout(function() { 
        emitter.emit('some_event'); 
}, 1000);





emitter.on('someEvent', function(arg1, arg2) {
        console.log('listener1', arg1, arg2);
});
emitter.on('someEvent', function(arg1, arg2) {
        console.log('listener2', arg1, arg2);
});
emitter.emit('someEvent', 'arg1 参数', 'arg2 参数');


emitter.once('init', () => {
  console.log('Initialization event occurred');
});

emitter.emit('init'); // 打印: Initialization event occurred
emitter.emit('init'); // 不会再触发


