import BusinessBO from "../bo/BusinessBO"

export default interface UpdateBusinessReq {
    data: UpdateBusinessReqBody
}

interface UpdateBusinessReqBody {
    businessBOList: BusinessBO[]
}