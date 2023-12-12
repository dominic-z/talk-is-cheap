import React, { ReactNode } from "react";
import { CSSTransition } from 'react-transition-group';
import "../css/Mask.css"

interface Params {
    showMask: boolean,
    content?: ReactNode
}

export default function MaskContainer(params: Params) {
    const mask = (
        <CSSTransition
            in={params.showMask}
            timeout={1000} classNames="mask"
            unmountOnExit={true}
            onEnter={(node: HTMLElement, isAppearing: boolean) => {
                // 因为CSSTransition是三个类之间的转换，而mask遮罩的一些样式需要在这三个类里同时体现，不是很好维护
                // 因此抽取公共内容，造了个mask-common，在enter的时候将这个类添加进去，整个动画周期都会生效
                // 仅仅将动画相关的样式抽取出来放在-enter，enter-active和enter-done里。
                node.classList.add("mask-common")
            }}
        >
            {/* 这块需要用div包裹一下，因为我发现CSSTransition本身会生成一个div（称之为A），
            但这个A和A内部的第一个ReactNode融合成一个div（称之为B）
                比如说，下方最外层的<div>组件里的class有个test类，那么CSSTransition生成的div（就是B）也会有test这个类。
            */}
            <div className="test">{params?.content}</div>
        </CSSTransition>
    )
    return mask;
}