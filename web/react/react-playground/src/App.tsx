import React from 'react';
import logo from './logo.svg';
import './App.css';
import CSSTransitionDemo from './component/react-transition/CSSTransitionDemo';
import SwitchAnimation from './component/react-transition/SwitchAnimation';
import TransitionBlock from './component/css/TransitionBlock';
import EventAPublisher from './component/events/EventAPublisher';
import EventAHandler from './component/events/EventAHandler';
import EEIndex from './component/eventemitter3/EEIndex';
import Parent from './component/lifecycle/parent';
import KeyDemo from './component/key/KeyDemo';
import KeyDemo2 from './component/key/KeyDemo2';
import KeyDemo3 from './component/key/KeyDemo3';
import UseEffectDemo1 from './component/useXXX/UseEffectDemo1';
import FormDemo1 from './component/antdDemo/FormDemo1';
import List from './component/basic/List';

function App() {


  return (
    // <TransitionBlock></TransitionBlock>
    // <CSSTransitionDemo></CSSTransitionDemo>
    // <SwitchAnimation></SwitchAnimation>
    // <>
    // <EventAPublisher></EventAPublisher>
    // <EventAHandler></EventAHandler>
    // </>

    // <EEIndex></EEIndex>
    // <Parent />
    // <KeyDemo />
    // <KeyDemo2 />
    // <KeyDemo3 />
    // <UseEffectDemo1 />

    // <FormDemo1 />
    <List></List>
  )
  // return (
  //   <div className="App">
  //     <header className="App-header">
  //       <img src={logo} className="App-logo" alt="logo" />
  //       <p>
  //         Edit <code>src/App.tsx</code> and save to reload.
  //       </p>
  //       <a
  //         className="App-link"
  //         href="https://reactjs.org"
  //         target="_blank"
  //         rel="noopener noreferrer"
  //       >
  //         Learn React
  //       </a>
  //     </header>
  //   </div>
  // );
}

export default App;
