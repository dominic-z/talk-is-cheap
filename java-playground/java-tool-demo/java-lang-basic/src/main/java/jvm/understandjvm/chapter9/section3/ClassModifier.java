package jvm.understandjvm.chapter9.section3;

/**
 * @author dominiczhu
 * @version 1.0
 * @title ClassModifier
 * @date 2021/10/23 上午11:30
 */
public class ClassModifier {

    /**
     * Class文件中常量池的起始偏移
     */
    private static final int CONSTANT_POOL_COUNT_INDEX = 8;
    /**
     * CONSTANT_Utf8_info常量的tag标志
     */
    private static final int CONSTANT_Utf8_info = 1;
    /**
     * 常量池中11种常量所占的长度，CONSTANT_Utf8_info型常量除外，因为它不是定长的
     * 这些常见常量info里，只有tag=1的字符串常量是不定长的，其余的都定长
     */
    private static final int[] CONSTANT_ITEM_LENGTH = { -1, -1, -1, 5, 5, 9, 9, 3, 3, 5, 5, 5, 5 };
    private static final int u1 = 1;
    private static final int u2 = 2;
    private byte[] classByte;
    public ClassModifier(byte[] classByte) {
        this.classByte = classByte;
    }
    /**
     * 修改常量池中CONSTANT_Utf8_info常量的内容
     * @param oldStr 修改前的字符串
     * @param newStr 修改后的字符串
     * @return 修改结果
     */
    public byte[] modifyUTF8Constant(String oldStr, String newStr) {
        int cpc = getConstantPoolCount();
        int offset = CONSTANT_POOL_COUNT_INDEX + u2;
        for (int i = 0; i < cpc; i++) {
            // 遍历每个常量，获取每个常量的tag
            int tag = ByteUtils.bytes2Int(classByte, offset, u1);
            if (tag == CONSTANT_Utf8_info) {
                // 获取字符串长度，长度记录在tag之后
                int len = ByteUtils.bytes2Int(classByte, offset + u1, u2);
                // 指针跳过tag与长度字段
                offset += (u1 + u2);

                // 将字符串常量的字符串数组转字符串
                String str = ByteUtils.bytes2String(classByte, offset, len);
                if (str.equalsIgnoreCase(oldStr)) {
                    byte[] strBytes = ByteUtils.string2Bytes(newStr);
                    byte[] strLen = ByteUtils.int2Bytes(newStr.length(), u2);
                    // 先替换长度
                    classByte = ByteUtils.bytesReplace(classByte, offset - u2, u2, strLen);
                    // 后将字符串替换为newStr
                    classByte = ByteUtils.bytesReplace(classByte, offset, len, strBytes);
                    return classByte;
                } else {
                    offset += len;
                }
            } else {
                // 如果非utf8常量，就跳过当前常量，当前常量大小通过CONSTANT_ITEM_LENGTH来跳过
                offset += CONSTANT_ITEM_LENGTH[tag];
            }
        }
        return classByte;
    }
    /**
     * 获取常量池中常量的数量
     * @return 常量池数量
     */
    public int getConstantPoolCount() {
        return ByteUtils.bytes2Int(classByte, CONSTANT_POOL_COUNT_INDEX, u2);
    }

}
