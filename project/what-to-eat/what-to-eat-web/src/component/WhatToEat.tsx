
import React from 'react';
import '../css/WhatToEat.css'
import BusinessForm from './BusinessForm';
import BusinessList from './BusinessList';
import CalenderContainer from './CalendarContainer';


export default function WhatToEat() {
    return (
        <div className='whatToEat'>
            <div className='mask'>
                <BusinessForm></BusinessForm>
            </div>
            <BusinessList></BusinessList>
            <CalenderContainer></CalenderContainer>
        </div>
    )
}


