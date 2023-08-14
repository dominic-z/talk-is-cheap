package org.talk.is.cheap.project.what.to.eat.domain.message;

import lombok.Data;

@Data
public class GenericRequest<T> {

    T data;
}
