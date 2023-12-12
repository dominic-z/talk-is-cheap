
import GenericResponse from "./GenericResponse";

export default interface CreateBusinessResp extends GenericResponse<UpdateBusinessRespBody> {
    data: UpdateBusinessRespBody
}

interface UpdateBusinessRespBody {
    
}