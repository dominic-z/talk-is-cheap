import { PlusOutlined } from '@ant-design/icons';
import { FloatButton } from 'antd';
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

interface Props {

}
// const url = "/business"
export default function BusinessList(props: Props) {

    const [businessBOs, setBusinessBOs] = useState<BusinessBO[]>([]);
    // 利用useRef产生的副作用来在不同渲染之间保存值以便用于重新刷新后重新请求数据。

    useEffect(
        () => {
            let ignore = false;
            function getBusinessList(page: number = 1, pageSize: number = 10) {

                if (page <= 0 || pageSize <= 0) {
                    console.log('illegal page ', page, ' or pageSize ', pageSize);
                }
                AxiosUtil.get<GetBusinessListResp>(BUSINESS_URL,
                    {
                        params: {
                            page: (page > 0) ? page : 1,
                            pageSize: (pageSize > 0) ? pageSize : 10
                        }
                    }
                )
                    .then(resp => {
                        if (!ignore) {
                            console.log(resp.data.data)
                            setBusinessBOs(resp.data.data.businessBOs)
                        }
                    }).catch(e => {
                        console.log(e);
                    })
            }

            EE.on(REFRESH_BUSINESS_LIST_EVENT, getBusinessList);
            EE.emit(REFRESH_BUSINESS_LIST_EVENT, 1, 10);

            return () => { ignore = true };
        }, [])


    const businessComponents = businessBOs.map(
        b => {
            return <Business key={b.id}
                businessId={b.id}
                name={b.name} description={b.description} imgUrl={""}></Business>
        }
    )


    const [showMask, setShowMask] = useState<boolean>(false)



    function onFinish(values: BusinessFormFieldType) {
        console.log('Success:', values);
        const req: CreateBusinessReq = {
            data: {
                name: values.name,
                description: values.description
            }
        }

        AxiosUtil.post<CreateBusinessResp>("/business", req)
            .then(resp => {
                console.log(resp)
                EE.emit(REFRESH_BUSINESS_LIST_EVENT)
            })
            .catch(e => {
                console.error(e)
            })
    }

    const onFinishFailed = (errorInfo: any) => {
        console.log('Failed:', errorInfo);
    };


    const businessForm = (
        <BusinessForm
            onFinish={onFinish}
            onCancel={() => setShowMask(false)}
            onFinishFailed={onFinishFailed}></BusinessForm>
    )
    const mask = (
        <MaskContainer showMask={showMask} content={businessForm}></MaskContainer>
    )

    const businessList = (
        <div className='businessListContainer'>
            <div className='businessList'>
                {businessComponents}
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
            {businessList}
        </>
    )

}


