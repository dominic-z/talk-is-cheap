
import React from 'react';
import '../css/Canvas.css'
import BusinessList from './BusinessList';
import CalenderContainer from './CalendarContainer';


export default function Canvas(){
    return (
        <div className='Canvas'>
            <BusinessList></BusinessList>
            <CalenderContainer></CalenderContainer>
        </div>
    )
}