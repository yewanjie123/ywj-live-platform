package com.ywj.live.generator.interfaces;

public interface IdGeneratorRpc {

    /**
     * 获取有序id
     *
     * @param id
     * @return
     */
    Long getSeqId(Integer id);

    /**
     * 获取无序id
     *
     * @param id
     * @return
     */
    Long getUnSeqId(Integer id);
}