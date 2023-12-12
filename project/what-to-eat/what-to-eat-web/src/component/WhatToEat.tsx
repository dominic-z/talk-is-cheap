
import React, { useRef, useState } from 'react';
import '../css/WhatToEat.css'
import "../css/Mask.css"
import BusinessForm from './BusinessForm';
import BusinessList from './BusinessList';
import CalenderContainer from './CalendarContainer';
import { CSSTransition } from 'react-transition-group';
import MaskContainer from './MaskContainer';
export default function WhatToEat() {

    return (
        <div className='whatToEat'>

            <BusinessList></BusinessList>
            <CalenderContainer></CalenderContainer>
        </div>
    )
}


