<script setup>
// 简单的通过响应式来修改图，因为vueflow可能对vue的响应式有点不太兼容，个人不推荐
import { ref } from 'vue'
import { VueFlow, Panel } from '@vue-flow/core'

const nodes = ref([
  {
    id: '1',
    position: { x: 50, y: 50 },
    data: {
      label: 'Node 1',
      hello: 'world',
    }
  },
])

function onSomeEvent(nodeId) {
  const node = nodes.value.find((node) => node.id === nodeId)

  // this won't work
  node.data = {
    hello: 'world',
    label: 'new'
  }
  // but this will work
  // node.data.label="new"

  // // you can also mutate properties like `selectable` or `draggable`
  // node.selectable = false
  // node.draggable = false

}
</script>

<template>
  <div>
    {{ nodes }}
  </div>

  <div :style="{ height: '400px', width: '400px' }">
    <!-- 这里要通过v-model来绑定，而不能通过v-bind来绑定 -->
    <VueFlow v-model:nodes="nodes">
      <Panel>
        <button type="button" @click="onSomeEvent('1')">Update Node 1</button>
      </Panel>
    </VueFlow>
  </div>

</template>