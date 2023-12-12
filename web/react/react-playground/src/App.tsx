import React from 'react';
import logo from './logo.svg';
import './App.css';
import CSSTransitionDemo from './component/react-transition/CSSTransitionDemo';
import SwitchAnimation from './component/react-transition/SwitchAnimation';
import TransitionBlock from './component/css/TransitionBlock';
import EventAPublisher from './component/events/EventAPublisher';
import EventAHandler from './component/events/EventAHandler';
import EEIndex from './component/eventemitter3/EEIndex';

function App() {


  return (
    // <TransitionBlock></TransitionBlock>
    // <CSSTransitionDemo></CSSTransitionDemo>
    // <SwitchAnimation></SwitchAnimation>
    // <>
    // <EventAPublisher></EventAPublisher>
    // <EventAHandler></EventAHandler>
    // </>

    <EEIndex></EEIndex>
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
