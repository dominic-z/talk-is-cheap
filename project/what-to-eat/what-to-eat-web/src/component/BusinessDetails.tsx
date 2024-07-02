import { Card, Image, List } from "antd";
import "../css/BusinessDetails.css"

interface Props {

}

export default function BusinessDetails(props: Props) {

    const data = Array.from({ length: 23 }).map((v, idx) => {
        return {
            key: idx,
            title: `小狗的第${idx}天`,
            content: `今天是第${idx}天，我很开心`
        }
    });

    console.log("list refresh")

    const imgUrl = 'https://ts1.cn.mm.bing.net/th/id/R-C.0bab160015bf9e56450d5fded33bd448?rik=VFMstq3mAZG1pg&riu=http%3a%2f%2fwww.qzqn8.com%2fwp-content%2fuploads%2f2020%2f02%2f3-9.jpg&ehk=pnkhUlGCDYmxisEuCwYL4zXW92froAxt%2f0B5i9AYAkY%3d&risl=&pid=ImgRaw&r=0&sres=1&sresct=1'


    const list = <List itemLayout="vertical" size="large"

        pagination={{
            onChange: (page, pageSize) => {
                console.log(`page:${page},pageSize:${pageSize}`)
            },
            pageSize: 3,
            showSizeChanger: true,
            pageSizeOptions: [3, 10, 20],
            size:'small',
            align: 'center',
            simple: true
        }}
        dataSource={data}
        renderItem={(item, idx) => {

            return <List.Item key={item.title} style={{ padding: 0, margin: "5px 2px" }}>
                <Card styles={{ body: { display: 'flex' } }}>
                    {/* 调整Card的body部分的样式，官网文档有写 */}
                    <Card.Meta title={item.title} description={item.content}></Card.Meta>
                    <Image width={100} src={imgUrl}  style={{borderRadius:'10px'}}/>
                </Card>
            </List.Item>
            // return <List.Item key={item.title} extra={<img width={100} alt="logo" src={imgUrl} />}>
            //     <List.Item.Meta title={item.title}/>
            //     {item.content}
            // </List.Item>
        }
        }
    ></List>

    return <div className="BusinessDetails">
        {list}
    </div>;
}