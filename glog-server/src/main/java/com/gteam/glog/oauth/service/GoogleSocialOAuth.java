package com.gteam.glog.oauth.service;

import com.gteam.glog.domain.dto.UserInfoDTO;
import com.gteam.glog.domain.dto.oauth.GoogleOAuthUserInfoResponseDTO;
import com.gteam.glog.domain.entity.Mypage;
import com.gteam.glog.domain.entity.Users;
import com.gteam.glog.domain.enums.SocialLoginType;
import com.gteam.glog.domain.dto.oauth.GoogleOAuthRequestDTO;
import com.gteam.glog.domain.dto.oauth.GoogleOAuthResponseDTO;
import com.gteam.glog.domain.entity.OAuthInfo;
import com.gteam.glog.login.repository.LoginRepository;
import com.gteam.glog.mypage.repository.MyPageRepository;
import com.gteam.glog.oauth.repository.OAuthRepository;
import com.gteam.glog.register.repository.RegisterRepository;
import lombok.extern.log4j.Log4j2;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Optional;


@Service
@Log4j2
public class GoogleSocialOAuth implements SocialOAuth {
    @Value("oauth.redirectionRootUrl")
    private String redirectionRootUrl;
    private OkHttpClient okHttpClient;
    private RestTemplate restTemplate;
    private final LoginRepository loginRepository;
    private final RegisterRepository registerRepository;
    private final MyPageRepository myPageRepository;
    private final OAuthRepository oAuthRepository;

    @Autowired
    public GoogleSocialOAuth(LoginRepository loginRepository, RegisterRepository registerRepository, MyPageRepository myPageRepository, OAuthRepository oAuthRepository) {
        this.loginRepository = loginRepository;
        this.registerRepository = registerRepository;
        this.myPageRepository = myPageRepository;
        this.oAuthRepository = oAuthRepository;
        this.okHttpClient = new OkHttpClient();
        this.restTemplate = new RestTemplate();
    }

    /**
     * google Authorization ?????? ??????
     *
     * @param token
     * @return Authorization ??????
     */
    @Override
    public HttpEntity createHeaders(String token) {
        String authHeader = "Bearer " + token;
        return new HttpEntity(new HttpHeaders(){{
            set("Content-Type","application/json; charset=UTF-8");
            set("Authorization", authHeader);
        }});
    }

    /**
     * Google OAuth Request Access Token
     * ????????? Access Code??? ???????????? Access Token ??????
     *
     * @param code
     * @return Optional<GoogleOAuthResponseDTO>
     */
    @Override
    public Optional<GoogleOAuthResponseDTO> requestAccessToken(String code) {
        try {
            OAuthInfo oAuthInfo = oAuthRepository.FindUserOAuthCode(SocialLoginType.GOOGLE).get();
            GoogleOAuthRequestDTO.AccessToken googleOAuthRequestDTO = new GoogleOAuthRequestDTO.AccessToken(
                    oAuthInfo.getClientId(),
                    oAuthInfo.getClientSecret(),
                    code,
                    "authoriztion_code",
                    redirectionRootUrl+"/google/callback"
            );

            // TODO: restTemplate => okHttp2 ??? ?????? ?????????
            // send data id, secret key , access code , grant type, redirect url
                return Optional.of(restTemplate.postForObject(
                        oAuthInfo.getTokenUrl(),
                        googleOAuthRequestDTO,
                        GoogleOAuthResponseDTO.class));
        } catch (IllegalArgumentException e){
            log.error("Null Exception : "+ e.getMessage());
            return null;
        } catch ( HttpStatusCodeException e){
            log.error("Http Status Exception : " + e.getMessage());
            return null;
        }
    }


    /**
     * ????????? ????????? ??????
     * 1. Access Token??? ???????????? Google??? ????????? ?????? ??????
     *
     * @param oAuthResponseDTO
     * @return GoogleOAuthUserInfoResponseDTO
     */
    public GoogleOAuthUserInfoResponseDTO requestUserProfile(GoogleOAuthResponseDTO oAuthResponseDTO) {
        try{
            OAuthInfo oAuthInfo = oAuthRepository.FindUserOAuthCode(SocialLoginType.GOOGLE).get();
            ResponseEntity<GoogleOAuthUserInfoResponseDTO> response =  restTemplate.exchange(
                    oAuthInfo.getProfileUrl(),
                    HttpMethod.GET,
                    this.createHeaders(oAuthResponseDTO.getAccess_token()),
                    GoogleOAuthUserInfoResponseDTO.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                log.info("GoogleOauth request success");
                return response.getBody();
            } else {
                log.info("GoogleOauth request Failed");
                return null;
            }
        }catch (IllegalArgumentException e) {
            return null;
        }catch (HttpStatusCodeException e){
            return null;
        }
    }

    /**
     * ????????? ??????
     * 1. ?????? ??????????????? ??????
     * 2. ????????????????????? ????????? ?????? ??????
     * 3. ?????? ??????????????? ????????????
     *
     * @param userInfoResponseDTO
     * @return MyPage
     */
    public Boolean validateUsers(GoogleOAuthUserInfoResponseDTO userInfoResponseDTO) {
        if (userInfoResponseDTO != null){
            if (loginRepository.getUsersByUserId(userInfoResponseDTO.getId()).orElse(null).equals(null)) {
                this.saveGoogleUser(userInfoResponseDTO);
            }
            return true;
        }else {
            return false;
        }
    }

    /**
     * ?????? OAuth ?????? ??????
     *
     * @param googleOauthUser
     */
    public void saveGoogleUser(GoogleOAuthUserInfoResponseDTO googleOauthUser){
        registerRepository.createOatuhUserInfo(UserInfoDTO.builder()
                        .mail(googleOauthUser.getId())
                        .pwd("")
                        .nikNm(googleOauthUser.getName())
                        .glogTitle(googleOauthUser.getGiven_name()+".log")
                        .imgNm(googleOauthUser.getPicture())
                .build());
    }
}
