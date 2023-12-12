import { EditOutlined, EllipsisOutlined, SettingOutlined } from "@ant-design/icons";
import { Avatar, Card } from "antd";
import Meta from "antd/es/card/Meta";
import React, { useState } from "react";
import { CSSTransition } from 'react-transition-group'
import "./css/CSSTransitionDemo.css"

export default function CSSTransitionDemo() {
    const [show, setShow] = useState<boolean>(true);

    return (
        <div>
            <button onClick={() => setShow(!show)}>change</button>

            <CSSTransition
                in={show}
                timeout={1000}
                // appear // appear的话，进入页面的时候就会以动画的形式展示出来
                classNames="fade"
                unmountOnExit={true}
            >
                <div className="box">
                    CSSTransitionDemo
                </div>
            </CSSTransition>


            <CSSTransition in={show}
                classNames="card"
                timeout={1000}
                // 没有appear，所以这个元素就直接展示出来了，没有过渡效果
                unmountOnExit={false}
                onEnter={() => console.log("进入动画前")}
                onEntering={() => console.log("进入动画")}
                onEntered={() => console.log("进入动画后")}
                onExit={el => console.log("退出动画前")}
                onExiting={el => console.log("退出动画")}
                onExited={el => console.log("退出动画后")}
            >
                <Card
                    style={{ width: 300 }}
                    cover={
                        <img
                            alt="example"
                            src="https://gw.alipayobjects.com/zos/rmsportal/JiqGstEfoWAOHiTxclqi.png"
                        />
                    }
                    actions={[
                        <SettingOutlined key="setting" />,
                        <EditOutlined key="edit" />,
                        <EllipsisOutlined key="ellipsis" />,
                    ]}
                >
                    <Meta
                        avatar={<Avatar src="https://zos.alipayobjects.com/rmsportal/ODTLcjxAfvqbxHnVXCYX.png" />}
                        title="Card title"
                        description="This is the description"
                    />
                </Card>

            </CSSTransition>


            <div className="box">
                    CSSTransitionDemo
                </div>
        </div>
    )
}