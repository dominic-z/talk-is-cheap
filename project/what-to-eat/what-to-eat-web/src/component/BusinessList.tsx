import { PlusOutlined } from '@ant-design/icons';
import { FloatButton, List } from 'antd';
import React, { useEffect, useRef, useState } from 'react';
import AxiosUtil from '../config/AxiosUtil';
import '../css/BusinessList.css'
import BusinessBO from '../domain/bo/BusinessBO';
import GetBusinessListResp from '../domain/message/GetBusinessListResp';
import Business from './Business';
import BusinessForm from './BusinessForm';
import MaskContainer from './MaskContainer';
import { BUSINESS_URL as BUSINESS_URL } from '../config/Urls'
import EE, { REFRESH_BUSINESS_LIST_EVENT } from '../config/EE';

import { BusinessFormFieldType } from "./BusinessForm"
import CreateBusinessReq from '../domain/message/CreateBusinessReq';
import CreateBusinessResp from '../domain/message/CreateBusinessResp';

import { Button, Divider, notification, Space } from 'antd';

interface Props {

}
// const url = "/business"

type NotificationPlacement = 'topLeft' | 'topRight' | 'bottomLeft' | 'bottomRight'


// todo：还没搞分页
export default function BusinessList(props: Props) {

    let [api, contextHolder] = notification.useNotification({ stack: { threshold: 3 } });
    const [showMask, setShowMask] = useState<boolean>(false)
    const openNotification = (placement: NotificationPlacement, errorMsg: string) => {
        api.error({
            message: '创建小店错误',
            description: errorMsg,
            placement: placement,
            duration: 2
        })
    }

    function onFinish(values: BusinessFormFieldType) {
        console.log('Success:', values);
        const req: CreateBusinessReq = {
            data: {
                name: values.name,
                description: values.description
            }
        }

        // 模拟：比如说请求1秒后报错。
        return new Promise((resolve) => {
            setTimeout(resolve, 1000)
        }).then(
            () => AxiosUtil.post<CreateBusinessResp>("/business", req)
        ).then(resp => {
            console.log(resp)
            // 关闭遮罩
            setShowMask(false)
            EE.emit(REFRESH_BUSINESS_LIST_EVENT)
        })
            .catch(e => {
                console.log("error", e)
                openNotification('bottomLeft', e.toString())
            })

        // return AxiosUtil.post<CreateBusinessResp>("/business", req)
        //     .then(resp => {
        //         console.log(resp)
        //         // 关闭遮罩
        //         setShowMask(false)
        //         EE.emit(REFRESH_BUSINESS_LIST_EVENT)
        //     })
        //     .catch(e => {
        //         console.log("error", e)
        //         openNotification('bottomLeft', e.toString())
        //     })
    }

    const onFinishFailed = (errorInfo: any) => {
        console.log('Failed:', errorInfo);
    };


    const businessForm = <>
        {contextHolder}
        <BusinessForm
            onFinish={onFinish}
            onCancel={() => setShowMask(false)}
            onFinishFailed={onFinishFailed}></BusinessForm>
    </>


    const mask = (
        <MaskContainer showMask={showMask} content={businessForm}></MaskContainer>
    )
    

    const [page,setPage] = useState(1)
    const [pageSize,setPageSize] = useState(3)
    const [total,setTotal] = useState(3)
    const [businessBOs, setBusinessBOs] = useState<BusinessBO[]>([]);

    useEffect(
        () => {
            let ignore = false;
            function getBusinessList(page: number = 1, pageSize: number = 3) {

                if (page <= 0 || pageSize <= 0) {
                    console.log('illegal page ', page, ' or pageSize ', pageSize);
                }
                AxiosUtil.get<GetBusinessListResp>(BUSINESS_URL,
                    {
                        params: {
                            page: (page > 0) ? page : 1,
                            pageSize: (pageSize > 0) ? pageSize : 3
                        }
                    }
                )
                    .then(resp => {
                        if (!ignore) {
                            let body = resp.data
                            console.log(body.data)
                            setBusinessBOs(body.data.businessBOs)
                            setTotal(body.data.total)
                        }
                    }).catch(e => {
                        console.log(e);
                    })
            }

            // 创建一个REFRESH_BUSINESS_LIST_EVENT事件的监听回调，这样在其他功能中如果需要更新列表，
            // 就可以直接通过发送事件来完成
            EE.on(REFRESH_BUSINESS_LIST_EVENT, getBusinessList);
            EE.emit(REFRESH_BUSINESS_LIST_EVENT, page, pageSize);

            return () => { ignore = true };
        }, [page,pageSize])

    // const businessComponents = businessBOs.map(
    //     b => {
    //         return <Business key={b.id}
    //             businessId={b.id}
    //             name={b.name} description={b.description} imgUrl={""}></Business>
    //     }
    // )

    const businessList = <List itemLayout='vertical' dataSource={businessBOs}
        split={false}
        renderItem={(bo) =>
            <List.Item key={bo.id} style={{padding:0}}>
                <Business key={bo.id}
                    businessId={bo.id}
                    name={bo.name} description={bo.description} imgUrl={""}></Business>
            </List.Item>
        }

        pagination={{
            onChange: (page, pageSize) => {
                setPage(page)
                setPageSize(pageSize)
            },
            current:page,
            pageSize: pageSize,
            total: total,
            align: 'center',
            simple: true,
            size:'small',
            showSizeChanger: true,
            pageSizeOptions: [3, 10, 20]
        }}
    ></List>

    const businessListContainer = (
        <div className='businessListContainer'>
            <div className='businessList'>
                {businessList}
            </div>

            <FloatButton
                shape="square"
                type="primary"
                className='addBusinessButton'
                icon={<PlusOutlined />}
                onClick={(e) => { setShowMask(true) }}
            />

        </div>
    )

    // 以空标签来返回，不能加div，否则businessList的高度样式是100%，就没法撑满整个高度了
    return (
        <>
            {mask}
            {businessListContainer}
        </>
    )

}


