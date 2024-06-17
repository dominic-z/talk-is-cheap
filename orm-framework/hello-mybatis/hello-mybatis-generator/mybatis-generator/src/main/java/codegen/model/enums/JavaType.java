package codegen.model.enums;

import lombok.Getter;

public enum JavaType {

    StringType("String"),
    BigDecimalType("java.math.BigDecimal"),
    IntegerType("Integer"),
    LongType("Long"),
    DateType("Date"),
    ByteArrayType("Byte[]"),
    FloatType("Float"),
    DoubleType("Double"),
    ;
    @Getter
    public final String typeName;

    JavaType(String typeName) {
        this.typeName = typeName;
    }
}
