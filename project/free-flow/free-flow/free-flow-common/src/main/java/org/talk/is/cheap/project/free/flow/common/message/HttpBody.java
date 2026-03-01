package org.talk.is.cheap.project.free.flow.common.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HttpBody<T> {
    private Integer code;
    private String msg;
    private T data;

    public HttpBody(T data) {
        this.data = data;
    }


    public boolean isSuccess() {
        return ResultCode.SUCCESS.getCode().equals(code);
    }

    public void success(T data, String msg) {
        this.code = ResultCode.SUCCESS.getCode();
        this.data = data;
    }

    public void success(T data) {
        success(data, null);
    }

    public void success() {
        success(null, null);
    }

    public void fail(ResultCode resultCode, String msg) {
        this.code = resultCode.getCode();
        this.msg = msg;
    }
}
