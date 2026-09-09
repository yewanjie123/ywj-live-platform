package com.ywj.live.framework.datasource;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.utils.StringUtils;
import org.apache.shardingsphere.driver.jdbc.core.driver.ShardingSphereDriverURLProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Properties;

public class NacosDriverURLProvider implements ShardingSphereDriverURLProvider {

    private static Logger logger = LoggerFactory.getLogger(NacosDriverURLProvider.class);
    private static final String NACOS_TYPE = "nacos:";
    private static final String GROUP = "DEFAULT_GROUP";

    /**
     * @param url the driver url
     * (jdbc:shardingsphere:nacos:192.168.79.163:8848:ywj-db-sharding.yaml?username=nacos&&password=nacos&&namespace=ywj-live-project）
     * @return
     */
    @Override
    public boolean accept(String url) {
        return url != null && url.contains(NACOS_TYPE);
    }

    /**
     * 从url中获取到nacos的连接配置信息
     *
     * @param url （jdbc:shardingsphere:192.168.79.163:8848:bmc-db-sharding.yaml?username=nacos&&password=nacos&&namespace=ywj-live-project）
     * @return
     */
    @Override
    public byte[] getContent(final String url) {
        if (StringUtils.isEmpty(url)) {
            return null;
        }
        //得到例如：192.168.79.163:8848:ywj-db-sharding.yaml?username=nacos&&password=nacos&&namespace=ywj-live-project 格式的url
        String nacosUrl = url.substring(url.lastIndexOf(NACOS_TYPE) + NACOS_TYPE.length());
        /**
         * 得到三个字符串，分别是：
         * 192.168.79.163
         * 8848
         * ywj-db-sharding.yaml?username=nacos&&password=nacos&&namespace=ywj-live-project
         */
        String nacosStr[] = nacosUrl.split(":");
        String nacosFileStr = nacosStr[2];
        /**
         * 得到两个字符串
         * ywj-db-sharding.yaml
         * username=nacos&&password=nacos&&namespace=ywj-live-project
         */
        String nacosFileProp[] = nacosFileStr.split("\\?");
        String dataId = nacosFileProp[0];
        /**
         * 得到3个字符串
         * "username=nacos"
         * “password=nacos”
         * "namespace=ywj-live-project"
         */
        String acceptProp[] = nacosFileProp[1].split("&&");
        //这里获取到
        Properties properties = new Properties();
        // key:serverAddr  value:192.168.79.163:8848
        properties.setProperty(PropertyKeyConst.SERVER_ADDR, nacosStr[0] + ":" + nacosStr[1]);
        for (String propertyName : acceptProp) {
            String[] propertyItem = propertyName.split("=");
            String key = propertyItem[0];
            String value = propertyItem[1];
            if ("username".equals(key)) {
                properties.setProperty(PropertyKeyConst.USERNAME, value);
            } else if ("password".equals(key)) {
                properties.setProperty(PropertyKeyConst.PASSWORD, value);
            } else if ("namespace".equals(key)) {
                properties.setProperty(PropertyKeyConst.NAMESPACE, value);
            }
        }
        ConfigService configService = null;
        try {
            configService = NacosFactory.createConfigService(properties);
            String content = configService.getConfig(dataId, GROUP, 6000);
            logger.info(content);
            return content.getBytes();
        } catch (NacosException e) {
            throw new RuntimeException(e);
        }
    }

}