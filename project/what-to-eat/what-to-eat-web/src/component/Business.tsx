import React from 'react';
import "../css/Business.css"

interface Params{
    name:string,
    description:string,
    imgUrl:string
}

export default function Business(params:Params){
    const defaultImgUrl="https://ts1.cn.mm.bing.net/th/id/R-C.0bab160015bf9e56450d5fded33bd448?rik=VFMstq3mAZG1pg&riu=http%3a%2f%2fwww.qzqn8.com%2fwp-content%2fuploads%2f2020%2f02%2f3-9.jpg&ehk=pnkhUlGCDYmxisEuCwYL4zXW92froAxt%2f0B5i9AYAkY%3d&risl=&pid=ImgRaw&r=0&sres=1&sresct=1";
    const defaultName = "这是小猫店";
    const defaultDescription = "小猫店天天做活动，然后到处拉大便，拉得满屏幕都是";
    return (
        <div className='business'>
            <div className='avatar'>
                <img src={params.imgUrl?params.imgUrl:defaultImgUrl}></img>
            </div>

            <div className='businessInfo'>
                <div className='name'>
                    {params.name?params.name:defaultName}
                </div>
                <div className='description'>
                    {params.description?params.description:defaultDescription}
                </div>
            </div>
        </div>
    )
}