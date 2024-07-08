import { DeleteOutlined, EditOutlined } from '@ant-design/icons';
import { Avatar, Badge, Card, Image, Popconfirm, Popover } from 'antd';
import React, { useState } from 'react';
import AxiosUtil from '../config/AxiosUtil';
import EE, { REFRESH_BUSINESS_LIST_EVENT } from '../config/EE';
import { BUSINESS_URL as BUSINESS_URL } from '../config/Urls';
import "../css/Business.css"
import BusinessBO from '../domain/bo/BusinessBO';
import DeleteBusinessResp from '../domain/message/DeleteBusinessResponse';
import UpdateBusinessReq from '../domain/message/UpdateBusinessReq';
import UpdateBusinessResp from '../domain/message/UpdateBusinessResp';
import BusinessForm, { BusinessFormFieldType } from './BusinessForm';
import MaskContainer from './MaskContainer';

interface Params {
    businessId: number,
    name: string,
    description: string,
    imgUrl: string
}

export default function Business(params: Params) {
    const defaultImgUrl = "https://ts1.cn.mm.bing.net/th/id/R-C.0bab160015bf9e56450d5fded33bd448?rik=VFMstq3mAZG1pg&riu=http%3a%2f%2fwww.qzqn8.com%2fwp-content%2fuploads%2f2020%2f02%2f3-9.jpg&ehk=pnkhUlGCDYmxisEuCwYL4zXW92froAxt%2f0B5i9AYAkY%3d&risl=&pid=ImgRaw&r=0&sres=1&sresct=1";
    const defaultName = "这是小猫店";
    const defaultDescription = "小猫店天天做活动，然后到处拉大便，拉得满屏幕都是";

    // const m = <Image
    const avater = (
        <Badge count={1}>
            <Avatar shape='square'
                size={100}
                src={<Image src={params.imgUrl ? params.imgUrl : defaultImgUrl}/>}></Avatar>
        </Badge>
    )

    const [showMask, setShowMask] = useState<boolean>(false);


    function onFinish(values: BusinessFormFieldType) {
        console.log("update business", values)

        const bo: BusinessBO = {
            id: params.businessId,
            name: values.name,
            description: values.description ? values.description : ""
        }
        const updateBusinessReq: UpdateBusinessReq = {
            data: {
                businessBOList: [bo]
            }
        }

        AxiosUtil.put<UpdateBusinessResp>(BUSINESS_URL, updateBusinessReq)
            .then(resp => {
                if (resp.data.code === 0) {
                    console.log("update success")
                    EE.emit(REFRESH_BUSINESS_LIST_EVENT)
                    setShowMask(false)
                } else {
                    console.log("errro when update business", params.businessId, resp.data.message)
                }
            })
    }
    const onFinishFailed = (errorInfo: any) => {
        console.log('Failed:', errorInfo);
    };
    const maskContent = (
        <BusinessForm
            businessBO={{ id: params.businessId, name: params.name, description: params.description }}
            onFinish={onFinish} 
            onCancel={function (): void {
                setShowMask(false);
            }} 
            onFinishFailed={onFinishFailed}
            defaultSubmitValue="更新"
        ></BusinessForm>
    )

    const maskContainer = (
        <MaskContainer showMask={showMask} content={maskContent}></MaskContainer>
    )

    function onEdit(e: any) {
        console.log(e);
        setShowMask(true);
    }

    function onDelete() {
        return AxiosUtil.delete<DeleteBusinessResp>(BUSINESS_URL, {
            params: {
                id: params.businessId
            }
        })
            .then(resp => {
                if (resp.data.code === 0) {
                    console.log("delete success", resp);
                    // todo: 提示
                } else {
                    // todo：提示
                }
            })
            .catch(resp => {
                console.log(resp);
            })
    }

    return (
        <div>
            {maskContainer}
            <Card style={{ margin: 10 }} hoverable bordered={true}
                actions={[
                    <EditOutlined key="edit" onClick={onEdit} />,

                    <Popconfirm description="Are you sure to delete this task?"
                        trigger="click" title="删除商家" onConfirm={onDelete}>
                        <DeleteOutlined key="delete" />
                    </Popconfirm>
                ]}
            >
                <Card.Meta
                    avatar={avater}
                    title={params.name ? params.name : defaultName}
                    description={params.description ? (params.description.length>15? params.description.slice(0,15)+'......':params.description) : defaultDescription}
                />
            </Card>
        </div>


    )
}