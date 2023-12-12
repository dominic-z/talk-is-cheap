export default interface CreateBusinessReq{
    data:CreateBusinessReqBody
}

interface CreateBusinessReqBody{
    name:string
    description:string|undefined

}