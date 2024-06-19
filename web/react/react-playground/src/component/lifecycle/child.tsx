/* eslint-disable */

// tsx写法
import React, { Component } from 'react';
import { Button } from 'antd';

const childStyle = {
  padding: 20,
  margin: 20,
  backgroundColor: 'LightSkyBlue',
};

const NAME = 'Child 组件：';


// tsx写法

interface Props {
  count: number
}
interface State {
  counter: number
}

interface Snapshot {
  ss: string
}

// Props代表着本组件的入参，使用方法见Parent组件
// State代表本组件的state，可以类比于函数式写法的setState内容。
export default class Child extends Component<Props, State, Snapshot> {
  constructor(props: Props) {
    super(props);
    console.log(NAME, 'constructor', props);
    this.state = {
      counter: 0,
    };
  }

  static getDerivedStateFromProps(nextProps: Props, prevState: State) {
    // 当nextProps
    console.log(NAME, 'getDerivedStateFromProps', nextProps, prevState);

    if (nextProps.count % 2 === 0) {
      // 返回一个非null的对象，这个对象会用于直接更新本组件的state
      // 当本组件的入参是偶数的时候，那么本组件的counter就一直会是10
      return { counter: 10 }
    }
    return null;
  }

  componentDidMount() {
    console.log(NAME, 'componentDidMount');
  }

  shouldComponentUpdate(nextProps: Props, prevState: State) {
    console.log(NAME, 'shouldComponentUpdate');
    return true;
  }

  getSnapshotBeforeUpdate(nextProps: Props, prevState: State) {
    console.log(NAME, 'getSnapshotBeforeUpdate');
    let ss: Snapshot = {
      ss: "abujiga"
    }
    return ss;
  }

  componentDidUpdate(prevProps: Props, prevState: State, snapshot: Snapshot) {
    // 这个snapshot就是getSnapshotBeforeUpdate的返回值
    console.log(snapshot)
    console.log(NAME, 'componentDidUpdate');
  }

  componentWillUnmount() {
    console.log(NAME, 'componentWillUnmount');
  }

  changeCounter = () => {
    let { counter } = this.state;
    this.setState({
      counter: ++counter,
    });
  };

  render() {
    console.log(NAME, 'render');
    const { count } = this.props;
    const { counter } = this.state;
    return (
      <div style={childStyle}>
        <h3>子组件</h3>
        <p>父组件传过来的属性 count ： {count}</p>
        <p>子组件自身状态 counter ： {counter}</p>
        <Button onClick={this.changeCounter}>改变自身状态 counter</Button>
      </div>
    );
  }
}
