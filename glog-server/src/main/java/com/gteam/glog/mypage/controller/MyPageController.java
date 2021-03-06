package com.gteam.glog.mypage.controller;

import com.gteam.glog.common.utils.JWTTokenUtils;
import com.gteam.glog.common.utils.ResponseDTOUtils;
import com.gteam.glog.domain.dto.UserInfoDTO;
import com.gteam.glog.mypage.service.MyPageService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;

@RestController
@Valid
@Log4j2
public class MyPageController {
    private final JWTTokenUtils jwtTokenUtils;
    private final ResponseDTOUtils responseDTOUtils;
    private final MyPageService myPageService;
    @Autowired
    public MyPageController(JWTTokenUtils jwtTokenUtils, ResponseDTOUtils responseDTOUtils, MyPageService myPageService) {
        this.jwtTokenUtils = jwtTokenUtils;
        this.responseDTOUtils = responseDTOUtils;
        this.myPageService = myPageService;
    }

    /**
     * 유저 정보 조회
     *
     * @param authorization
     * @param mail
     * @return
     */
    @GetMapping("/myinfo")
    @ApiOperation(value = "유저 정보 조회", notes = "유저 정보를 요청하는 API")
    public ResponseEntity<?> getMyPage(@RequestHeader(value = "authorization")@Min(value = 255)String authorization,
                                       @RequestHeader(value = "X-USER-ID") @Email String mail,
                                       @CookieValue(value = "refresh")Cookie reqCookie) {
        log.info("--> getMypage : - {}",mail);
        return responseDTOUtils.doGenerateResponseDTO(myPageService.findUserInfoByUserId(mail));
    }


    /**
     *유저 정보 수정
     *
     * @param authorization
     * @param mail
     * @return
     */
    @PostMapping("/myinfo")
    @ApiOperation(value = "유저 정보 수정", notes = "유저 정보를 수정하는 API")
    public ResponseEntity<?> updateMyPage(@RequestHeader(value = "authorization")@Min(value = 255) String authorization,
                                          @RequestHeader(value = "X-USER-ID") @Email String mail,
                                          @CookieValue(value = "refresh")Cookie reqCookie,
                                          @RequestBody()UserInfoDTO userInfoDTO ) {
        log.info("--> updateMypage : - {}",mail);
        return responseDTOUtils.doGenerateResponseDTO(myPageService.saveUserInfo(mail,userInfoDTO));
    }
}
