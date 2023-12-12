
import GenericResponse from "./GenericResponse";

export default interface CreateBusinessResp extends GenericResponse<CreateBusinessRespBody> {
    data: CreateBusinessRespBody
}

interface CreateBusinessRespBody {
    id: number
}