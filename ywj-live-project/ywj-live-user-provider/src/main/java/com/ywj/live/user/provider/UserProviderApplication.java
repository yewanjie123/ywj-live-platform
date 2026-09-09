package com.ywj.live.user.provider;


import com.ywj.live.user.constants.UserTagsEnum;
import com.ywj.live.user.dto.UserDTO;
import com.ywj.live.user.dto.UserLoginDTO;
import com.ywj.live.user.provider.service.IUserPhoneService;
import com.ywj.live.user.provider.service.IUserService;
import com.ywj.live.user.provider.service.IUserTagService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDubbo
@EnableDiscoveryClient
public class UserProviderApplication implements CommandLineRunner {
    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(UserProviderApplication.class);
        // 应用程序不作为web应用启动，不启动内嵌的服务。
        application.setWebApplicationType(WebApplicationType.NONE);
        application.run(args);

    }

    @Resource
    private IUserTagService userTagService;

    @Resource
    private IUserService userService;

    @Resource
    private IUserPhoneService userPhoneService;

    @Override
    public void run(String... args) throws Exception {
//        long userId = 1004L;
//        UserDTO userDTO = userService.getByUserId(userId);
//        userDTO.setNickName("ella");
//        userService.updateUserInfo(userDTO);

//        System.out.println(userTagService.containTag(userId, UserTagsEnum.IS_OLD_USER));
//        System.out.println(userTagService.setTage(userId, UserTagsEnum.IS_OLD_USER));
//        System.out.println(userTagService.containTag(userId, UserTagsEnum.IS_OLD_USER));
//        System.out.println(userTagService.cancel(userId, UserTagsEnum.IS_OLD_USER));
//        System.out.println(userTagService.containTag(userId, UserTagsEnum.IS_OLD_USER));

        /*System.out.println(userTagService.cancel(userId, UserTagsEnum.IS_RICH));
        System.out.println("判断当前用户是否存在isRich标签:" + userTagService.containTag(userId,UserTagsEnum.IS_RICH));
        System.out.println(userTagService.cancel(userId, UserTagsEnum.IS_VIP));
        System.out.println("判断当前用户是否存在isVip标签:" + userTagService.containTag(userId,UserTagsEnum.IS_VIP));
        System.out.println(userTagService.cancel(userId, UserTagsEnum.IS_OLD_USER));
        System.out.println("判断当前用户是否存在isOldUser标签:" + userTagService.containTag(userId,UserTagsEnum.IS_OLD_USER));*/
        String phone = "13567098712";
        UserLoginDTO userLoginDTO = userPhoneService.login(phone);
        System.out.println(userLoginDTO);
        System.out.println(userPhoneService.queryByUserId(userLoginDTO.getUserId()));
        System.out.println(userPhoneService.queryByUserId(userLoginDTO.getUserId()));
        System.out.println(userPhoneService.queryByPhone(phone));
        System.out.println(userPhoneService.queryByPhone(phone));
    }
}